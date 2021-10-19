package com.zzy.diytomcat.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    // LinkedBlockingQueueは，初期状態の20スレッドが全部動いている場合にジョブが増えたら，まずはListに格納し，Listの中のジョブを優先的に処理する。
    // Listがいっぱいになったらスレッド数を増やす（ここでは最大100スレッドまで）。
    // 増えたスレッドが60秒空いている場合，回収され，20スレッドに戻る。
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10));

    public static void run(Runnable r){
        threadPool.execute(r);
    }
}
