package it.gov.pagopa.payment.notices.service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum AppError {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something was wrong"),
    BAD_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, "Bad Request", "%s"),
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


    COULD_NOT_GET_FILE_URL_ERROR(HttpStatus.NOT_FOUND,"Couldn't get file url",
            "Encountered an error recovering file url"),
    NOTICE_CLIENT_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE,
            "Notice Storage Not Available",
            "Notice Storage client temporarily not available"),

    ERROR_ON_GET_FILE_URL_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR,"Error occurred while attempting to get file url",
            "Error occurred while attempting to get file url"),

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


