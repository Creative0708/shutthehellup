package io.github.creative0708.shutthehellup.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.RejectedExecutionException;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> p_129518_, PacketListener p_129519_) {
    }

    @Shadow
    @Final
    private static Logger LOGGER;

    @Redirect(method = "doSendPacket(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Lnet/minecraft/network/ConnectionProtocol;Lnet/minecraft/network/ConnectionProtocol;)V", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;writeAndFlush(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;"))
    private ChannelFuture wrapWithTryCatch(Channel instance, Object o) {
        try {
            return instance.writeAndFlush(o);
        } catch (Exception e) {
            return new DefaultChannelPromise(instance);
        }
    }

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void wrapWithTryCatch(Packet<?> packet, PacketListener listener) {
        try {
            genericsFtw(packet, listener);
        } catch (RunningOnDifferentThreadException | RejectedExecutionException e) {
            // rethrow valid exceptions to not prevent e.g. server disconnecting
            throw e;
        } catch (Exception e) {
            LOGGER.error("Received {} that couldn't be processed", packet.getClass(), e);
        }
    }

}
