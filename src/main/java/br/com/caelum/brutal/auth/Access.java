package br.com.caelum.brutal.auth;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.brutal.dao.UserDAO;
import br.com.caelum.brutal.dto.UserAndSession;
import br.com.caelum.brutal.model.User;
import br.com.caelum.brutal.model.UserSession;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.ComponentFactory;

@Component
public class Access implements ComponentFactory<User> {
	
	public static final String BRUTAL_SESSION = "brutal_session";
    private UserAndSession userAndSession;
    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final UserDAO users; 
	
	public Access(HttpServletResponse response, HttpServletRequest request, UserDAO users) {
	    this.response = response;
        this.request = request;
        this.users = users;
    }

	public User login(User user) {
	    UserSession newSession = user.newSession();
	    Cookie cookie = new Cookie(BRUTAL_SESSION, newSession.getSessionKey());
	    cookie.setPath("/");
	    cookie.setHttpOnly(true);
	    cookie.setMaxAge(Integer.MAX_VALUE);
        response.addCookie(cookie);
		users.save(newSession);
		this.userAndSession = new UserAndSession(user, newSession);
		return user;
	}

	@Override
	public User getInstance() {
		User user = userAndSession == null ? null : userAndSession.getUser();
		return user;
	}
	
	@PostConstruct
	public boolean tryToAutoLogin() {
	    String key = extractKeyFromCookies();
	    if (key != null) {
	    	this.userAndSession = users.findBySessionKey(key);
	    }
	    return this.userAndSession != null;
	}
	
    private String extractKeyFromCookies() {
        Cookie[] cookiesArray = request.getCookies();
        if (cookiesArray != null) {
            List<Cookie> cookies = Arrays.asList(cookiesArray);
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(BRUTAL_SESSION)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

	public void logout() {
	    Cookie cookie = new Cookie(BRUTAL_SESSION, "");
	    cookie.setPath("/");
	    cookie.setMaxAge(-1);
	    response.addCookie(cookie);
	    users.delete(userAndSession.getUserSession());
		this.userAndSession = null;
	}
	

}
