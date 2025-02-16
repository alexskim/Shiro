package com.mikuac.shiro.handler.event;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.common.utils.EventUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.request.FriendAddRequestEvent;
import com.mikuac.shiro.dto.event.request.GroupAddRequestEvent;
import com.mikuac.shiro.enums.RequestEventEnum;
import com.mikuac.shiro.handler.injection.InjectionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author zero
 */
@Component
public class RequestEvent {

    private EventUtils utils;

    @Autowired
    public void setUtils(EventUtils utils) {
        this.utils = utils;
    }

    /**
     * 存储请求事件处理器
     */
    public final Map<String, BiConsumer<Bot, JSONObject>> handlers = new HashMap<>();


    private InjectionHandler injection;

    @Autowired
    public void setInjection(InjectionHandler injection) {
        this.injection = injection;
    }

    /**
     * 请求事件分发
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void handler(Bot bot, JSONObject resp) {
        String type = resp.getString("request_type");
        handlers.getOrDefault(
                type,
                (b, e) -> {
                }
        ).accept(bot, resp);
    }

    /**
     * 事件处理
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     * @param type {@link RequestEventEnum}
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored", "squid:S2201"})
    private void process(Bot bot, JSONObject resp, RequestEventEnum type) {
        if (type == RequestEventEnum.GROUP) {
            GroupAddRequestEvent event = resp.to(GroupAddRequestEvent.class);
            injection.invokeGroupAddRequest(bot, event);
            bot.getPluginList().stream().anyMatch(o -> utils.getPlugin(o).onGroupAddRequest(bot, event) == BotPlugin.MESSAGE_BLOCK);
        }
        if (type == RequestEventEnum.FRIEND) {
            FriendAddRequestEvent event = resp.to(FriendAddRequestEvent.class);
            injection.invokeFriendAddRequest(bot, event);
            bot.getPluginList().stream().anyMatch(o -> utils.getPlugin(o).onFriendAddRequest(bot, event) == BotPlugin.MESSAGE_BLOCK);
        }
    }

    /**
     * 加好友请求
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void friend(Bot bot, JSONObject resp) {
        process(bot, resp, RequestEventEnum.FRIEND);
    }

    /**
     * 加群请求
     *
     * @param bot  {@link Bot}
     * @param resp {@link JSONObject}
     */
    public void group(Bot bot, JSONObject resp) {
        process(bot, resp, RequestEventEnum.GROUP);
    }

}
