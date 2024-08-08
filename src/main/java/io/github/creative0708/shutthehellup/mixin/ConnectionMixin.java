package io.github.creative0708.shutthehellup.mixin;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.RejectedExecutionException;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> p_129518_, PacketListener p_129519_) {
    }

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "lambda$doSendPacket$9", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketSendListener;onFailure()Lnet/minecraft/network/protocol/Packet; "), cancellable = true)
    private void ignoreErrors(PacketSendListener p_243290_, Future<?> p_243167_, CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "doSendPacket", at = @At(value = "INVOKE", target = "Lio/netty/channel/ChannelFuture;addListener(Lio/netty/util/concurrent/GenericFutureListener;)Lio/netty/channel/ChannelFuture;", ordinal = 1))
    private ChannelFuture dontFireExceptionOnFailure(ChannelFuture instance, GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
        return instance;
    }

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void wrapWithTryCatch(Packet<?> packet, PacketListener listener) {
        try {
            genericsFtw(packet, listener);
        } catch (RunningOnDifferentThreadException | RejectedExecutionException e) {
            // rethrow valid exceptions to not prevent e.g. server disconnecting
            throw e;
        } catch (Throwable e) {
            LOGGER.error("Received {} that couldn't be processed", packet.getClass(), e);
        }
    }

}
