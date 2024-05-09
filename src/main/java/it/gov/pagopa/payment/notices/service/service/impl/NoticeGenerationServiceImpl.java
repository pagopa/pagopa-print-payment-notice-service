package it.gov.pagopa.payment.notices.service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import it.gov.pagopa.payment.notices.service.client.NoticeGenerationClient;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequest;
import it.gov.pagopa.payment.notices.service.entity.PaymentNoticeGenerationRequestError;
import it.gov.pagopa.payment.notices.service.events.NoticeGenerationRequestProducer;
import it.gov.pagopa.payment.notices.service.exception.Aes256Exception;
import it.gov.pagopa.payment.notices.service.exception.AppError;
import it.gov.pagopa.payment.notices.service.exception.AppException;
import it.gov.pagopa.payment.notices.service.model.*;
import it.gov.pagopa.payment.notices.service.model.enums.PaymentGenerationRequestStatus;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestErrorRepository;
import it.gov.pagopa.payment.notices.service.repository.PaymentGenerationRequestRepository;
import it.gov.pagopa.payment.notices.service.service.NoticeGenerationService;
import it.gov.pagopa.payment.notices.service.storage.NoticeStorageClient;
import it.gov.pagopa.payment.notices.service.util.Aes256Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.payment.notices.service.util.WorkingDirectoryUtils.createWorkingDirectory;

/**
 * @see it.gov.pagopa.payment.notices.service.service.NoticeGenerationService
 */
@Service
@Slf4j
public class NoticeGenerationServiceImpl implements NoticeGenerationService {

    private final PaymentGenerationRequestRepository paymentGenerationRequestRepository;
    private final PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository;

    private final NoticeGenerationRequestProducer noticeGenerationRequestProducer;

    private final ObjectMapper objectMapper;

    private final Aes256Utils aes256Utils;

    private final NoticeGenerationClient noticeGenerationClient;

    private final NoticeStorageClient noticeStorageClient;

    public NoticeGenerationServiceImpl(
            PaymentGenerationRequestRepository paymentGenerationRequestRepository,
            PaymentGenerationRequestErrorRepository paymentGenerationRequestErrorRepository,
            NoticeGenerationRequestProducer noticeGenerationRequestProducer,
            ObjectMapper objectMapper, Aes256Utils aes256Utils,
            NoticeGenerationClient noticeGenerationClient,
            NoticeStorageClient noticeStorageClient) {
        this.paymentGenerationRequestRepository = paymentGenerationRequestRepository;
        this.paymentGenerationRequestErrorRepository = paymentGenerationRequestErrorRepository;
        this.noticeGenerationRequestProducer = noticeGenerationRequestProducer;
        this.objectMapper = objectMapper;
        this.aes256Utils = aes256Utils;
        this.noticeGenerationClient = noticeGenerationClient;
        this.noticeStorageClient = noticeStorageClient;
    }

    @Override
    public GetGenerationRequestStatusResource getFolderStatus(String folderId, String userId) {
        Optional<PaymentNoticeGenerationRequest> paymentNoticeGenerationRequestOptional =
                paymentGenerationRequestRepository.findByIdAndUserId(folderId, userId);
        if(paymentNoticeGenerationRequestOptional.isEmpty()) {
            throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
        }
        List<String> errors = paymentGenerationRequestErrorRepository.findErrors(folderId)
                .stream().map(PaymentNoticeGenerationRequestError::getId).toList();
        PaymentNoticeGenerationRequest paymentNoticeGenerationRequest = paymentNoticeGenerationRequestOptional.get();
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
            String folderId =
                    paymentGenerationRequestRepository.save(
                            PaymentNoticeGenerationRequest.builder()
                                    .status(PaymentGenerationRequestStatus.INSERTED)
                                    .createdAt(Instant.now())
                                    .items(new ArrayList<>())
                                    .userId(userId)
                                    .numberOfElementsTotal(noticeGenerationMassiveRequest.getNotices().size())
                                    .requestDate(Instant.now())
                                    .build()).getId();

            sendNotices(noticeGenerationMassiveRequest, folderId);

            return folderId;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_MASSIVE_GENERATION_REQUEST);
        }

    }

    @Override
    public File generateNotice(NoticeGenerationRequestItem noticeGenerationRequestItem, String folderId, String userId) {
        try {

            if(folderId != null && userId != null) {
                Optional<PaymentNoticeGenerationRequest> paymentNoticeGenerationRequestOptional =
                        paymentGenerationRequestRepository.findByIdAndUserId(folderId, userId);
                if(paymentNoticeGenerationRequestOptional.isEmpty()) {
                    throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
                }
            }

            File workingDirectory = createWorkingDirectory();
            Path tempDirectory = Files.createTempDirectory(workingDirectory.toPath(), "notice-generation-service")
                    .normalize().toAbsolutePath();

            Response generationResponse = noticeGenerationClient.generateNotice(folderId, noticeGenerationRequestItem);

            if(generationResponse.status() != HttpStatus.OK.value()) {
                throw new RuntimeException("Client Exception");
            }

            try (InputStream inputStream = generationResponse.body().asInputStream()) {
                File targetFile = File.createTempFile("tempFile", ".pdf", tempDirectory.toFile());
                FileUtils.copyInputStreamToFile(inputStream, targetFile);
                return targetFile;
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GENERATION_REQUEST);
        }
    }

    @Override
    public GetSignedUrlResource getFileSignedUrl(String folderId, String fileId, String userId) {

//        Optional<PaymentNoticeGenerationRequest> paymentNoticeGenerationRequestOptional =
//                paymentGenerationRequestRepository.findByIdAndUserId(folderId, userId);
//        if (paymentNoticeGenerationRequestOptional.isEmpty()) {
//            throw new AppException(AppError.FOLDER_NOT_AVAILABLE);
//        }

        try {
            return GetSignedUrlResource.builder()
                    .signedUrl(noticeStorageClient.getFileSignedUrl(folderId, fileId))
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException(AppError.ERROR_ON_GET_FILE_URL_REQUEST);
        }
    }

    @Async
    public void sendNotices(NoticeGenerationMassiveRequest noticeGenerationMassiveRequest, String folderId) {
        noticeGenerationMassiveRequest.getNotices().parallelStream().forEach(noticeGenerationRequestItem -> {
            try {
                if(!noticeGenerationRequestProducer.noticeGeneration(
                        NoticeGenerationRequestEH.builder()
                                .noticeData(noticeGenerationRequestItem)
                                .folderId(folderId)
                                .build())
                ) {
                    saveErrorEvent(folderId, noticeGenerationRequestItem);
                }
            } catch (Exception e) {
                saveErrorEvent(folderId, noticeGenerationRequestItem);
            }
        });
    }

    private void saveErrorEvent(String folderId, NoticeGenerationRequestItem noticeGenerationRequestItem) {
        try {
            paymentGenerationRequestErrorRepository.save(
                    PaymentNoticeGenerationRequestError.builder()
                            .errorDescription("Encountered error sending notice on EH")
                            .folderId(folderId)
                            .data(aes256Utils.encrypt(objectMapper
                                    .writeValueAsString(noticeGenerationRequestItem)))
                            .createdAt(Instant.now())
                            .numberOfAttempts(0)
                            .build()
            );
            paymentGenerationRequestRepository.findAndIncrementNumberOfElementsFailedById(folderId);
        } catch (JsonProcessingException | Aes256Exception e) {
            log.error(
                    "Unable to save notice data into error repository for notice with folder " + folderId +
                            " and noticeId " + noticeGenerationRequestItem.getData().getNotice().getCode()
            );
        }
    }

}
