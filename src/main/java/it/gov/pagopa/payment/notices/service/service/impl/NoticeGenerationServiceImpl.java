package it.gov.pagopa.payment.notices.service.service.impl;

import feign.Response;
import it.gov.pagopa.payment.notices.service.client.NoticeGenerationClient;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.GetGenerationRequestStatusResource;
import it.gov.pagopa.payment.notices.service.model.GetSignedUrlResource;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationMassiveRequest;
import it.gov.pagopa.payment.notices.service.model.NoticeGenerationRequestItem;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import it.gov.pagopa.payment.notices.service.service.BrokerService;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.service.async.AsyncService;
import it.gov.pagopa.payment.notices.service.storage.NoticeStorageClient;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.payment.notices.service.util.WorkingDirectoryUtils.createWorkingDirectory;

/**
 * @see it.gov.pagopa.payment.notices.service.service.NoticeGenerationService
 */
@Service
@Slf4j
public class NoticeGenerationServiceImpl implements NoticeGenerationService {

    private final PaymentGenerationRequestRepository paymentGenerationRequestRepository;
    private final PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;
    private final NoticeGenerationClient noticeGenerationClient;
    private final AsyncService asyncService;

    private final BrokerService brokerService;

    private final NoticeStorageClient noticeStorageClient;

    public NoticeGenerationServiceImpl(
            PaymentGenerationRequestRepository paymentGenerationRequestRepository,
            PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository,
            AsyncService asyncService,
            NoticeGenerationClient noticeGenerationClient,
            BrokerService brokerService, NoticeStorageClient noticeStorageClient) {
        this.paymentGenerationRequestRepository = paymentGenerationRequestRepository;
        this.paymentGenerationRequestErrorRepository = paymentGenerationRequestErrorRepository;
        this.asyncService = asyncService;
        this.noticeGenerationClient = noticeGenerationClient;
        this.brokerService = brokerService;
        this.noticeStorageClient = noticeStorageClient;
    }

    @Override
    public GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId) {
        var paymentNoticeGenerationRequest = findFolderIfExists(folderId, userId);

        List<String> errors = paymentGenerationRequestErrorRepository.findErrors(folderId)
                .stream()
                .filter(item -> !item.isCompressionError())
                .map(PaymentNoticeGenerationRequestError::getId)
                .toList();

        return GetGenerationRequestStatusResource
                .builder()
                .status(paymentNoticeGenerationRequest.getStatus())
                .processedNotices(paymentNoticeGenerationRequest.getItems())
                .noticesInError(errors)
                .build();
    }

    @Override
    @Transactional
    public String generateMassive(NoticeGenerationMassiveRequest noticeGenerationMassiveRequest, String userId) {

        try {


            String folderId = paymentGenerationRequestRepository.save(PaymentNoticeGenerationRequest.builder()
                    .status(PaymentGenerationRequestStatus.INSERTED)
                    .createdAt(Instant.now())
                    .items(new ArrayList<>())
                    .userId(userId)
                    .numberOfElementsTotal(noticeGenerationMassiveRequest.getNotices().size())
                    .numberOfElementsFailed(0)
                    .requestDate(Instant.now())
                    .build()).getId();

            asyncService.sendNotices(noticeGenerationMassiveRequest, folderId, userId);

            return folderId;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_MASSIVE_GENERATION_REQUEST, e);
        }

    }

    @Override
    public File generateNotice(NoticeGenerationRequestItem noticeGenerationRequestItem, String folderId, String userId) {
        try {

            String ciTaxCode = noticeGenerationRequestItem.getData().getCreditorInstitution().getTaxCode();

            if(!userId.equals(ciTaxCode) && !brokerService.checkBrokerAllowance(userId, ciTaxCode,
                    noticeGenerationRequestItem.getData().getNotice().getCode())) {
                throw new AppException(AppError.NOT_ALLOWED_ON_CI_CODE);
            }

            if(folderId != null) {
                findFolderIfExists(folderId, userId);
            }

            File workingDirectory = createWorkingDirectory();
            Path tempDirectory = Files.createTempDirectory(workingDirectory.toPath(), "notice-generation-service")
                    .normalize()
                    .toAbsolutePath();

            try (Response generationResponse = noticeGenerationClient.generateNotice(folderId, noticeGenerationRequestItem)) {
                if(generationResponse.status() != HttpStatus.OK.value()) {
                    log.error("Feign Client Response {}", generationResponse);
                    throw new AppException(AppError.NOTICE_GEN_CLIENT_ERROR);
                }

                try (InputStream inputStream = generationResponse.body().asInputStream()) {
                    File targetFile = File.createTempFile("tempFile", ".pdf", tempDirectory.toFile());
                    FileUtils.copyInputStreamToFile(inputStream, targetFile);
                    return targetFile;
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GENERATION_REQUEST, e);
        }
    }

    @Override
    public GetSignedUrlResource getFileSignedUrl(String folderId, String fileId, String userId) {

        findFolderIfExists(folderId, userId);

        try {
            return GetSignedUrlResource.builder()
                    .signedUrl(noticeStorageClient.getFileSignedUrl(folderId, fileId))
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GET_FILE_URL_REQUEST);
        }

    }

    @Override
    @Transactional
    public void deleteFolder(String folderId, String userId) {
        findFolderIfExists(folderId, userId);
        try {
            paymentGenerationRequestRepository.deleteById(folderId);
            noticeStorageClient.deleteFolder(folderId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GET_FILE_URL_REQUEST);
        }

    }

    @Override
    public GetSignedUrlResource getFolderSignedUrl(String folderId, String userId) {

        PaymentNoticeGenerationRequest paymentNoticeGenerationRequest = findFolderIfExists(folderId, userId);
        if(!PaymentGenerationRequestStatus.PROCESSED.equals(paymentNoticeGenerationRequest.getStatus())) {
            throw new AppException(AppError.NOTICE_REQUEST_YET_TO_PROCESS);
        }

        try {
            return GetSignedUrlResource.builder()
                    .signedUrl(noticeStorageClient.getFileSignedUrl(folderId, folderId.concat(".zip")))
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GET_FOLDER_URL_REQUEST);
        }

    }


    private PaymentNoticeGenerationRequest findFolderIfExists(String folderId, String userId) {
        return paymentGenerationRequestRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new AppException(AppError.FOLDER_NOT_AVAILABLE));
    }

}
