package cmb.custody.configuration.infrastructure.common.web;

import cmb.custody.configuration.infrastructure.common.util.ErrorCode;
import cmb.custody.monitor.infrastructure.exceptions.MonitorBussException;
import cmb.custody.monitor.infrastructure.exceptions.MonitorSystException;
import com.cmb.bee.commons.exceptions.BizException;
import com.cmb.bee.commons.exceptions.SysException;
import com.cmb.bee.web.advice.BeeWebExceptionHandlerAdvice;
import com.cmb.bee.web.annotations.BeeResponseBody;
import com.cmb.bee.web.entity.BeeResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义全局异常处理
 * 基础类型的全局处理参见{@link BeeWebExceptionHandlerAdvice}
 * 使用@Order(Ordered.HIGHEST_PRECEDENCE)设置最高优先级
 *
 * @author 80280674
 * @date 2022/11/18
 */
// 设置最高优先级
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    BeeWebExceptionHandlerAdvice beeWebExceptionHandlerAdvice;

    @ExceptionHandler(MonitorBussException.class)
    @ResponseBody
    public Object handleMonitorBussException(MonitorBussException e) {
        log.error("处理请求异常[业务异常]", e);
        return BeeResponseEntity.failed(ErrorCode.TJK9999, e.getMessage());
    }

    @ExceptionHandler(MonitorSystException.class)
    @ResponseBody
    public Object handleMonitorSystException(MonitorSystException e) {
        log.error("处理请求异常[系统异常]", e);
        return BeeResponseEntity.failed(ErrorCode.TJK9998, e.getMessage());
    }

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Object handleBizException(BizException e) {
        log.error("处理请求异常[业务异常]", e);
        return BeeResponseEntity.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(SysException.class)
    @ResponseBody
    public Object handleSysException(SysException e) {
        log.error("处理请求异常[系统异常]", e);
        return BeeResponseEntity.failed(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object handleException(Exception e) {
        log.error("处理请求异常[通用异常]", e);
        return BeeResponseEntity.failed(ErrorCode.TJK9990, e.getMessage());
    }
}