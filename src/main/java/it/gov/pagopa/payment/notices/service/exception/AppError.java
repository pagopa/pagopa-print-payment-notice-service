package it.gov.pagopa.payment.notices.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum AppError {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something was wrong"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad Request", "%s"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized", "Error during authentication"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Forbidden", "This method is forbidden"),
    RESPONSE_NOT_READABLE(HttpStatus.BAD_GATEWAY, "Response Not Readable", "The response body is not readable"),

    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "Template Not Found",
            "Required template has not been found on the storage"),
    TEMPLATE_CLIENT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "Template Storage Not Available",
            "Template Storage client temporarily not available"),
    TEMPLATE_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Template Client Error",
            "Template Client encountered an error"),
    TEMPLATE_TABLE_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Template Table Client Error",
            "Template Table Client encountered an error"),
    FOLDER_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Folder Not Available",
            "Required folder is either missing or not available to the requirer"),
    ERROR_ON_MASSIVE_GENERATION_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR,
            "Exception on Massive Generation Request",
            "Encountered a blocking exception on the massive generation request"),
    ERROR_ON_GENERATION_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR,
            "Exception on Generation Request",
            "Encountered a blocking exception on the generation request"),

    NOTICE_GEN_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Exception on generation client", "Exception on generation client"),


    COULD_NOT_GET_FILE_URL_ERROR(HttpStatus.NOT_FOUND, "Couldn't get file url",
            "Encountered an error recovering file url"),
    NOTICE_CLIENT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "Notice Storage Not Available",
            "Notice Storage client temporarily not available"),

    ERROR_ON_GET_FILE_URL_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while attempting to get file url",
            "Error occurred while attempting to get file url"),

    COULD_NOT_DELETE_FOLDER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error deleting notices", "Error occurred while attempting to delete a folder"),

    NOTICE_REQUEST_YET_TO_PROCESS(HttpStatus.FORBIDDEN, "Notice request To Be Completed",
            "The notice request is yet to be completely processed"),

    ERROR_ON_GET_FOLDER_URL_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error recovering folder Signed Url",
            "Error occured while attempting to retrieve folder signed url"),
    INSTITUTION_DATA_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error uploading institution data",
            "Error occured while attempting to upload institution data"),

    LOGO_FILE_INPUT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error managing logo input",
            "Exception has been thrown while managing the logo file passed as input," +
                    " could not create either the working directory or the file"),

    INSTITUTION_CLIENT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "Institution Storage Not Available",
            "Institution Storage client temporarily not available"),

    INSTITUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Institution Not Found",
            "Required institution data has not been found on the storage"),

    INSTITUTION_PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Parsing Error for Institution Data",
            "Exception thrown while parsing institution data retrieve from storage"),

    INSTITUTION_RETRIEVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Error while retrieving Institution data",
            "Unexpected error occurred while retrieving institution data"),

    NOT_ALLOWED_ON_CI_CODE(HttpStatus.FORBIDDEN, "Not allowed for notice ci tax code", "The request has been executed for a creditor institution having a different tax code to the one onboarded, and without having allowance to manage notices for said ci"),

    ERROR_ON_PT_ALLOWANCE_CHECK(HttpStatus.INTERNAL_SERVER_ERROR,
            "Error on pt allowance check", "Error occured during allowance check for PT"),

    UNKNOWN(null, null, null);


    public final HttpStatus httpStatus;
    public final String title;
    public final String details;


    AppError(HttpStatus httpStatus, String title, String details) {
        this.httpStatus = httpStatus;
        this.title = title;
        this.details = details;
    }
}


