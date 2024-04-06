package com.jones.tank.config;


import com.jones.tank.object.BaseResponse;
import com.jones.tank.object.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

//import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;


@ControllerAdvice(annotations = {RestController.class})
@Slf4j
public class ControllerAdvise {

	/**
	 * json格式请求 RequestBody valid报错
	 * 
	 * @param ex exception
	 * @return response
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@Order(400)
	public ResponseEntity<BaseResponse> handleValidationException(
			MethodArgumentNotValidException ex) {
		log.warn(ex.getMessage(), ex);

		return new ResponseEntity<>(new BaseResponse(ErrorCode.VALIDATION_FAILED,
				ex.getBindingResult().getAllErrors().stream().map(err -> err.getDefaultMessage())
						.collect(Collectors.joining(","))),
				HttpStatus.BAD_REQUEST);
	}

//	@ExceptionHandler(ConstraintViolationException.class)
//	@Order(401)
//	public ResponseEntity<BaseResponse> handleValidationException(ConstraintViolationException ex) {
//		log.warn(ex.getMessage(), ex);
//
//		return new ResponseEntity<>(
//				new BaseResponse(ErrorCode.VALIDATION_FAILED, ex.getConstraintViolations().stream()
//						.map(cons -> cons.getMessage()).collect(Collectors.joining(","))),
//				HttpStatus.BAD_REQUEST);
//	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<BaseResponse> handleException(RuntimeException ex) {
		log.error(ex.getMessage(), ex);
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		return new ResponseEntity<>(new BaseResponse(ErrorCode.INTERNAL_ERROR, ex.getMessage()),
				status);
	}

}
