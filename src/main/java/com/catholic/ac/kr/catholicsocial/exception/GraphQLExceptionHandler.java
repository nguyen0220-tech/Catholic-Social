package com.catholic.ac.kr.catholicsocial.exception;

import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import lombok.NonNull;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment environment) {
        // Kiểm tra xem lỗi có phải là GraphQLException đã ném ra không
        if (ex instanceof GraphQLException) {
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage()) //Nội dung lỗi gửi cho client
                    .path(environment.getExecutionStepInfo().getPath()) // Đường dẫn field lỗi
                    .location(environment.getField().getSourceLocation()) //Vị trí field trong query GraphQL --> debug
                    .build();
        }

        if (ex instanceof ConstraintViolationException cve){
            String message = cve.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath()+":"+v.getMessage())
                    .findFirst()
                    .orElse("Validation Error");

            /*
            getPropertyPath(): Trả về đường dẫn field bị lỗi

            findFirst(): Lấy phần tử đầu tiên trong stream ->  Trả về: Optional<String>
            Vì sao chỉ lấy 1 lỗi?
                    GraphQL thường hiển thị 1 lỗi / field
                    FE dễ xử lý
                    Tránh trả quá nhiều message
             */
            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(message)
                    .path(environment.getExecutionStepInfo().getPath())
                    .build();
        }

        // Nếu là các lỗi khác, có thể để null để Spring xử lý mặc định hoặc custom tiếp
        return null;
    }
}
