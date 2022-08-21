package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 过滤器
 * 检查用后是否已经完成了登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();
        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//移动端发送短信
                "/user/login"//移动端登录
        };
        //2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);
        //3.如果不需要处理，则直接放行
        if (check) {
            filterChain.doFilter(request, response);
            return;
        }
        Long empId = (Long) request.getSession().getAttribute("employee");
        //4-1.判断登录状态，如已登录，则直接放行
        if (empId != null) {

//            long id = Thread.currentThread().getId();
//            log.info("doFilter线程id为:{}"+id);
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        Long userId = (Long) request.getSession().getAttribute("user");
        //4-2.判断登录状态，如已登录，则直接放行，移动端
        if (userId != null) {

//            long id = Thread.currentThread().getId();
//            log.info("doFilter线程id为:{}"+id);
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        //5.如未登录则直接放回结果，通过输出流向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 检测本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match == true) {
                return true;
            }
        }
        return false;

    }


}
