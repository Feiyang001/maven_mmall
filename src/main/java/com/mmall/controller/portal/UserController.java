package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 *
 */
@Controller
@RequestMapping("/user/") //请求的地址全部放在/user这个命名的空间下写在类上
public class UserController {

    @Autowired
    private IUserService iUserService;
    /**
     * 用户登录功能模块
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)// 指定请求的方法
    @ResponseBody() //这个注解是在返回的时候自动通过springMVC的的jkson插件将我们的返回值序列化为json
    public ServerResponse<User> login(String username, String password, HttpSession session){
        //调用service层
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 退出登录的接口
     * @param session
     * @return
     */
    @RequestMapping(value="logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        //从session里把currentUser 删除就可以了
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.creatBySuccess();
    }

    /**
     * 用户注册校验的接口
     * @param
     * @return
     */
    @RequestMapping(value="register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){

        return iUserService.register(user);
    }

    /**
     * 校验用户名和email是否存在 防止恶意的用户通过接口调用注册接口
     * @param str   value 的值
     * @param type 通过type  username 和email
     * @return
     */
    @RequestMapping(value="check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
        return iUserService.checkValid(str,type);
    }

    /**
     * 获取用户信息的方法
     * @param session
     * @return
     */
    @RequestMapping(value="get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<User> getUserInfo(HttpSession session){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if (user!=null){
            return ServerResponse.creatBySuccess(user);
        }
        return ServerResponse.creatByErrorMessage("用户未登录，无法获取用户信息");
    }

    /**
     * 忘记密码后得到密码的提示问题
     * @param
     * @return
     */
    @RequestMapping(value="forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return  iUserService.selectQuestion(username);
    }

    /**
     * 获取提示问题与答案 （用本地的guava缓存来做token，利用token的有效期来完成）
     * 使用本地缓存来检查问题答案的接口
     * @param username 用户名
     * @param question 提示问题
     * @param answer 答案
     * @return
     */
    @RequestMapping(value="forget_check_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question ,String answer){

        return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 忘记密码的重置密码的接口
     * @param username  用户
     * @param newPassword 新密码
     * @Param forgetToken
     * @return
     */
    @RequestMapping(value="forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String newPassword, String forgetToken){
        return  iUserService.forgerResetPassword(username,newPassword,forgetToken);
    }

    /**
     * 登录状态下的修改密码的接口
     * @param session 确定用户是登录状态 或者说是从session中获取用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return
     */
    @RequestMapping(value="reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String oldPassword,String newPassword){
        //从session中拿到正在登录的用户
        User user=(User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.creatByErrorMessage("用户未登录");
        }

        return iUserService.resetPassword(oldPassword,newPassword,user);
    }

    /**
     * 更新用户的信息，更新完成之后存在session中，可以显示到前端的页面之中
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value="update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        //从session中拿到正在登录的用户
        User currentUser=(User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return ServerResponse.creatByErrorMessage("用户未登录");
        }
        //user传guo来的信息中是没有id信息，需要把当前登录的id赋值给user
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    @RequestMapping(value="get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session){
        //从session中拿到正在登录的用户
        User currentUser=(User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser==null){
            return ServerResponse.creatByErrorCoderMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，需要强制登录");
        }
        return iUserService.getInformation(currentUser.getId());
    }

}
