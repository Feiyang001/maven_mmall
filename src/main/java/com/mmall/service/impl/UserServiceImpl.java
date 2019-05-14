package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.common.ServerResponse;
import com.mmall.service.IUserService;

import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")  //声明为一个service 名字是iUserService
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 业务层实现登录的方法
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {

        int resultCount=userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.creatByErrorMessage("用户名不存在");
        }
        // to do 一个密码登录MD5 注册的时候是对密码进行了加密，所以比较的时候也是要比较加密的密码
        String md5Password=MD5Util.MD5EncodeUtf8(password);

        User user=userMapper.selectLogin(username,md5Password);
        if(user==null){
            return ServerResponse.creatByErrorMessage("密码错误");
        }

        //处理返回值的密码 密码志为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.creatBySuccess("登录成功",user);
    }

    /**
     * 业务层实现校验的方法
     * 注册时候的实时校验
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {

        /*int resultCount=userMapper.checkUsername(user.getUsername());
        if (resultCount>0){
            return ServerResponse.creatByErrorMessage("用户名存在");
        }
         resultCount=userMapper.checkEmail(user.getEmail());
        if (resultCount>0){
            return ServerResponse.creatByErrorMessage("用户名存在");
        }

        */

        ServerResponse validResponse=this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse=this.checkValid(user.getUsername(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密 需要一个MD5的加密类
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount=userMapper.insert(user);
        if (resultCount==0){
            return ServerResponse.creatByErrorMessage("注册失败");
        }

        return ServerResponse.creatBySuccessMessage("注册成功");
    }

    /**
     * 校验用户名和email是否存在 防止恶意的用户通过接口调用注册接口
     * @param str   value 的值
     * @param type 通过type  username 和email
     * @return
     */
    public ServerResponse<String> checkValid(String str, String type){

        if (StringUtils.isNoneBlank(type)){ //判断传过来的type不是空的
            //开始校验
            if(Const.USERNAME.equals(type)){
                //小通过用户名校验用户是否存在
                int resultCount=userMapper.checkUsername(str);
                if (resultCount>0){
                    return ServerResponse.creatByErrorMessage("用户名存在");
                }
            }
            if (Const.EMAIL.equals(type)){
                int resultCount=userMapper.checkEmail(str);
                if (resultCount>0) {
                    return ServerResponse.creatByErrorMessage("email存在");
                }
            }
        }else{
            return  ServerResponse.creatByErrorMessage("参数错误");
        }
        return ServerResponse.creatBySuccessMessage("校验成功");
    }

    /**
     * 忘记密码的提示问题的方法
     * @param username
     * @return
     */
    public ServerResponse selectQuestion(String username){

        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.creatByErrorMessage("用户不存在");
        }
        String question=userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)){
            return  ServerResponse.creatBySuccess(question);
        }
        return ServerResponse.creatByErrorMessage("找回密码的问题不存在") ;
    }

    /**
     * 获取印证问题和答案的接口
     * @param username
     * @param question
     * @param answer
     * @return
     */
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount=userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明用户是存在的，问题和问题的答案是正确的
            String forgetToken= UUID.randomUUID().toString();//这个方法得到的是一个基本不会重复的字符串
            //处理forgetToken把其放在本地的cache中，设置有效期
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return  ServerResponse.creatBySuccess(forgetToken);
        }
        return ServerResponse.creatByErrorMessage("问题的答案错误");
    }

    /**
     * 业务层中处理忘记密码重置密码的接口
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    public ServerResponse<String> forgerResetPassword(String username,String newPassword,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServerResponse.creatByErrorMessage("参数错误，请传递有效的token");
        }
        //校验用户
        ServerResponse validResponse=this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.creatByErrorMessage("用户不存在");
        }
        String token=TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return  ServerResponse.creatByErrorMessage("token无效或者过期");
        }
        if (StringUtils.equals(forgetToken,token)){
            String md5Password=MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount= userMapper.updatePasswordByUsername(username,md5Password);
            if (resultCount>0){
                return ServerResponse.creatBySuccessMessage("密码重置成功");
            }
        }else{
            return ServerResponse.creatByErrorMessage("token错误，请重新获取重置秘密的token");
        }
            return ServerResponse.creatByErrorMessage("修改密码失败");
    }

    /**
     * 业务层中的用户登录状态下的重置密码的接口
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param user 当前用户
     * @return
     */
    public ServerResponse<String> resetPassword(String oldPassword,String newPassword ,User user){
        //防止横向越权，一定要校验这个用户的旧密码一定要指向这个用户。会查询一个count。如果不指定id，count是true
        int resultCount=userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if (resultCount==0){
            return ServerResponse.creatByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        //设置完更新一下
        int updateCount =userMapper.updateByPrimaryKeySelective(user);
        if (updateCount>0){
            return ServerResponse.creatBySuccessMessage("密码重置成功");
        }
        return ServerResponse.creatByErrorMessage("密码重置失败");
    }

    /**
     * 业务层中出来更新用户信息的接口
     * @param user
     * @return
     */
    public ServerResponse<User>  updateInformation(User user){
        //更新信息的时候姓名不用更新，得校验email是否存在，不包含此用户的校验
        int resultCount= userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return  ServerResponse.creatByErrorMessage("邮箱已经存在，请重新输入邮箱");
        }

        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount>0){
            return ServerResponse.creatBySuccess("个人信息已更新",updateUser);
        }
        return ServerResponse.creatByErrorMessage("用户信息更新失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user=userMapper.selectByPrimaryKey(userId);
        if(user==null){
            return ServerResponse.creatByErrorMessage("当前用户不存在");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.creatBySuccess(user);
    }
}
