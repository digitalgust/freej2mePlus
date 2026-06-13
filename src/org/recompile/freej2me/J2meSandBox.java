package org.recompile.freej2me;

/**
 * 把FreeJ2ME 变成一个沙盒，以便可以运行多个 FreeJ2ME
 * FreeJ2ME 一开始就会创建一个线程，此线程会运行一个特定的ThreadGroup，由此线程组限定这个FreeJ2ME的运行环境
 * 核心要解决的是，把所有事件都打包后，交给FreeJ2ME来处理，这样，就把所有事件都由当前的FreeJ2ME处理，
 *
 */
public class J2meSandBox {
    public void notifyDestroy() {
    }

    public boolean isEventThread() {
        return false;
    }

    public void requestRepaint() {

    }

    public void addEvent(Runnable runnable) {
    }
}
