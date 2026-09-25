package com.clothsell.module.mall.service.auth;

import com.clothsell.framework.security.core.TokenService;
import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.module.mall.dal.dataobject.auth.LoginLogDO;
import com.clothsell.module.mall.dal.dataobject.auth.MallAdminDO;
import com.clothsell.module.mall.dal.dataobject.auth.MallUserDO;
import com.clothsell.module.mall.dal.dataobject.auth.RefreshTokenDO;
import com.clothsell.module.mall.dal.mysql.auth.LoginLogMapper;
import com.clothsell.module.mall.dal.mysql.auth.MallAdminMapper;
import com.clothsell.module.mall.dal.mysql.auth.MallUserMapper;
import com.clothsell.module.mall.dal.mysql.auth.RefreshTokenMapper;
import com.clothsell.module.mall.vo.auth.AdminLoginReqVO;
import com.clothsell.module.mall.vo.auth.ClientLoginReqVO;
import com.clothsell.module.mall.vo.auth.LoginRespVO;
import com.clothsell.module.mall.vo.auth.RefreshReqVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static com.clothsell.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.LOGIN_BAD;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.TOKEN_EXPIRED;
import static com.clothsell.module.mall.enums.ErrorCodeConstants.USER_EXISTS;

@Service
@Validated
public class AuthService {
    @Resource
    private MallUserMapper mallUserMapper;
    @Resource
    private MallAdminMapper mallAdminMapper;
    @Resource
    private TokenService tokenService;
    @Resource
    private LoginLogMapper loginLogMapper;
    @Resource
    private RefreshTokenMapper refreshTokenMapper;
    @Resource
    private LoginGuard loginGuard;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private static final String DUMMY_HASH = new BCryptPasswordEncoder().encode("cloth-sell-login-dummy");

    public LoginRespVO register(ClientLoginReqVO reqVO) {
        if (reqVO.getPassword().length() < 6) {
            throw exception(LOGIN_BAD);
        }
        if (mallUserMapper.selectByPhone(reqVO.getPhone()) != null) {
            throw exception(USER_EXISTS);
        }
        MallUserDO user = new MallUserDO();
        user.setPhone(reqVO.getPhone());
        user.setPassword(encoder.encode(reqVO.getPassword()));
        mallUserMapper.insert(user);
        return session("USER", user.getId(), user.getPhone());
    }

    public LoginRespVO login(ClientLoginReqVO reqVO) {
        loginGuard.check(reqVO.getPhone());
        MallUserDO user = mallUserMapper.selectByPhone(reqVO.getPhone());
        if (!passwordMatches(user == null ? null : user.getPassword(), reqVO.getPassword())) {
            loginGuard.fail(reqVO.getPhone());
            writeLog("USER", user == null ? null : user.getId(), reqVO.getPhone(), false);
            throw exception(LOGIN_BAD);
        }
        loginGuard.ok(reqVO.getPhone());
        writeLog("USER", user.getId(), user.getPhone(), true);
        return session("USER", user.getId(), user.getPhone());
    }

    public LoginRespVO adminLogin(AdminLoginReqVO reqVO) {
        loginGuard.check(reqVO.getUsername());
        MallAdminDO admin = mallAdminMapper.selectByUsername(reqVO.getUsername());
        if (!passwordMatches(admin == null ? null : admin.getPassword(), reqVO.getPassword())) {
            loginGuard.fail(reqVO.getUsername());
            writeLog("ADMIN", admin == null ? null : admin.getId(), reqVO.getUsername(), false);
            throw exception(LOGIN_BAD);
        }
        loginGuard.ok(reqVO.getUsername());
        writeLog("ADMIN", admin.getId(), admin.getUsername(), true);
        return session("ADMIN", admin.getId(), admin.getUsername());
    }

    @Transactional
    public LoginRespVO refresh(RefreshReqVO reqVO) {
        RefreshTokenDO row = refreshTokenMapper.selectByHash(sha256(reqVO.getRefreshToken()));
        if (row == null || row.getExpireTime().isBefore(LocalDateTime.now())) {
            throw exception(TOKEN_EXPIRED);
        }
        if (refreshTokenMapper.deleteById(row.getId()) != 1) {
            throw exception(TOKEN_EXPIRED);
        }
        return session(row.getUserType(), row.getUserId(), row.getAccount());
    }

    public void logout(RefreshReqVO reqVO) {
        if (reqVO.getRefreshToken() == null || reqVO.getRefreshToken().isBlank()) {
            return;
        }
        RefreshTokenDO row = refreshTokenMapper.selectByHash(sha256(reqVO.getRefreshToken()));
        if (row != null) {
            refreshTokenMapper.deleteById(row.getId());
        }
    }

    private LoginRespVO session(String role, Long id, String name) {
        LoginRespVO resp = new LoginRespVO();
        String accessToken = tokenService.issueAccess(new LoginUser(id, role, name));
        resp.setAccessToken(accessToken);
        resp.setToken(accessToken);
        resp.setRefreshToken(saveRefresh(role, id, name));
        resp.setName(name);
        resp.setPermissions("ADMIN".equals(role) ? adminPerms() : clientPerms());
        return resp;
    }

    private String saveRefresh(String role, Long id, String name) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshTokenDO row = new RefreshTokenDO();
        row.setUserType(role);
        row.setUserId(id);
        row.setAccount(name);
        row.setTokenHash(sha256(raw));
        row.setExpireTime(LocalDateTime.now().plusDays(7).withNano(0));
        refreshTokenMapper.insert(row);
        return raw;
    }

    private boolean passwordMatches(String hash, String raw) {
        return encoder.matches(raw, hash == null ? DUMMY_HASH : hash) && hash != null;
    }

    private String sha256(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                hex.append(String.format("%02x", item & 0xff));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> adminPerms() {
        return List.of(
                "mall:product:query", "mall:product:create", "mall:product:update", "mall:product:delete",
                "mall:order:query", "mall:order:update");
    }

    private List<String> clientPerms() {
        return List.of(
                "mall:client-product:query",
                "mall:client-cart:query", "mall:client-cart:create", "mall:client-cart:update", "mall:client-cart:delete",
                "mall:client-order:query", "mall:client-order:create", "mall:client-order:update");
    }

    private void writeLog(String userType, Long userId, String account, boolean success) {
        LoginLogDO row = new LoginLogDO();
        row.setUserType(userType);
        row.setUserId(userId);
        row.setAccount(account == null ? "" : account);
        row.setSuccess(success ? 1 : 0);
        HttpServletRequest request = currentRequest();
        if (request != null) {
            row.setUserIp(clientIp(request));
            String agent = request.getHeader("User-Agent");
            if (agent != null && agent.length() > 255) {
                agent = agent.substring(0, 255);
            }
            row.setUserAgent(agent);
        }
        loginLogMapper.insert(row);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String first = forwarded.split(",")[0].trim();
            return first.length() > 64 ? first.substring(0, 64) : first;
        }
        return request.getRemoteAddr();
    }
}
