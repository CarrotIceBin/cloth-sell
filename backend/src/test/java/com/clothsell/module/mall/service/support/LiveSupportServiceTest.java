package com.clothsell.module.mall.service.support;

import com.clothsell.framework.security.core.util.SecurityFrameworkUtils.LoginUser;
import com.clothsell.module.mall.dal.dataobject.support.SupportMessageDO;
import com.clothsell.module.mall.dal.mysql.support.SupportMessageMapper;
import com.clothsell.module.mall.websocket.SupportSocketHandler;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LiveSupportServiceTest {
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    void login(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new LoginUser(7L, role, "buyer"), null, java.util.List.of()));
    }
    @Test void buyerCannotSelectAnotherConversationOrEnterAdminWorkstation() {
        login("USER");
        assertEquals(7L, LiveSupportService.conversation(LiveSupportService.current(false), 99L));
        assertThrows(AccessDeniedException.class, () -> LiveSupportService.current(true));
        login("ADMIN");
        assertThrows(AccessDeniedException.class, () -> LiveSupportService.current(false));
    }
    @Test void validationRejectsEmptyLongOrInvalidMessages() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var uuid = java.util.UUID.randomUUID().toString();
            assertFalse(validator.validate(new LiveSupportService.SendRequest(null, uuid, " ")).isEmpty());
            assertFalse(validator.validate(new LiveSupportService.SendRequest(null, uuid, "x".repeat(2001))).isEmpty());
            assertFalse(validator.validate(new LiveSupportService.SendRequest(null, "bad", "hello")).isEmpty());
            assertTrue(validator.validate(new LiveSupportService.SendRequest(null, uuid, "hello")).isEmpty());
        }
    }
    @Test void duplicateSendReturnsSavedMessageWithoutPublishingAgain() {
        login("USER");
        var mapper = mock(SupportMessageMapper.class);
        var transactions = mock(TransactionTemplate.class);
        var sockets = mock(SupportSocketHandler.class);
        var saved = new SupportMessageDO();
        saved.setId(20L); saved.setUserId(7L); saved.setContent("hello");
        saved.setSenderRole("USER"); saved.setSenderName("buyer"); saved.setClientId("same");
        saved.setCreateTime(LocalDateTime.now());
        when(mapper.selectOne(any())).thenReturn(saved);
        var service = new LiveSupportService(mapper, transactions, sockets);
        assertEquals("20", service.send(false, new LiveSupportService.SendRequest(99L, "same", "hello")).id());
        verifyNoInteractions(transactions, sockets);
        assertThrows(com.clothsell.framework.common.exception.ServiceException.class,
                () -> service.send(false, new LiveSupportService.SendRequest(null, "same", "different")));
    }
    @Test void failedCommitDoesNotPushAndSuccessfulCommitPushesAfterwards() {
        login("USER");
        var mapper = mock(SupportMessageMapper.class);
        var transactions = mock(TransactionTemplate.class);
        var sockets = mock(SupportSocketHandler.class);
        var service = new LiveSupportService(mapper, transactions, sockets);
        when(transactions.execute(any())).thenThrow(new IllegalStateException("database unavailable"));
        assertThrows(IllegalStateException.class, () -> service.send(false,
                new LiveSupportService.SendRequest(null, "one", "hello")));
        verifyNoInteractions(sockets);
        var saved = new SupportMessageDO();
        saved.setId(1L); saved.setUserId(7L); saved.setContent("hello");
        doReturn(saved).when(transactions).execute(any());
        var message = service.send(false, new LiveSupportService.SendRequest(null, "two", "hello"));
        var order = inOrder(transactions, sockets);
        order.verify(transactions, times(2)).execute(any());
        order.verify(sockets).publish(7L, message);
    }
}
