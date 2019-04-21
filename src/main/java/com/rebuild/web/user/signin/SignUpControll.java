/*
rebuild - Building your business-systems freely.
Copyright (C) 2018 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.web.user.signin;

import java.awt.Font;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.rebuild.web.BasePageControll;
import com.wf.captcha.utils.CaptchaUtil;

/**
 * 注册
 * 
 * @author devezhao
 * @since 11/01/2018
 */
@Controller
@RequestMapping("/user/")
public class SignUpControll extends BasePageControll {
	
	@RequestMapping("signup")
	public ModelAndView pageSignup(HttpServletRequest request, HttpServletResponse response) {
		return createModelAndView("/user/signup.jsp");
	}
	
	@RequestMapping("captcha")
	public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Font font = new Font(Font.SERIF, Font.ITALIC, 20 + RandomUtils.nextInt(10));
		CaptchaUtil.outPng(160, 41, 6, font, request, response);
	}
}