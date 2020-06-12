package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnums;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LyException extends RuntimeException{

    private ExceptionEnums exceptionEnums;

}
