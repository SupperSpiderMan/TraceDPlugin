package com.jadyn.trace_extra.consumer

import com.jadyn.trace_extra.MethodTraceServerManager
import com.jadyn.trace_extra.MethodTraceServerManager.APPINFO
import com.jadyn.trace_extra.MethodTraceServerManager.METHODCOST
import com.jadyn.trace_extra.TraceManServer
import com.jadyn.trace_extra.utils.LogUtil
import com.jadyn.trace_extra.model.Message
import com.jadyn.trace_extra.producer.module.appInfo.AppInfoProducer
import com.jadyn.trace_extra.producer.module.methodcost.MethodCostProducer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class DataConsumer(var server: TraceManServer) {

    private var compositeDisposables: CompositeDisposable = CompositeDisposable()


    fun observe() {

        compositeDisposables.addAll(
            appInfoConsumer(),
            methodCostConsumer()
        )

    }

    private fun methodCostConsumer(): Disposable? {
        return MethodTraceServerManager.getModule<MethodCostProducer>(METHODCOST).subject()
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe({
                val messageEntity = Message(METHODCOST, it)
                val message = messageEntity.toString()
                server.sendMessage(message)

                //Log输出
                LogUtil.i("已向浏览器发送${it.size}条方法耗时信息")
                it.forEach { methodInfo ->
                    val threadText = if (methodInfo.isMainThread) "[主线程]" else "[非主线程]"
                    LogUtil.detail("方法耗时详情:" + methodInfo.name + "  " + methodInfo.costTime + "ms" + " " + threadText)
                }
                LogUtil.detail(message)
            }, {
                LogUtil.detail(it.message)
            })
    }


    private fun appInfoConsumer(): Disposable? {
        return MethodTraceServerManager.getModule<AppInfoProducer>(APPINFO).subject()
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.computation())
            .subscribe({
                val messageEntity = Message(APPINFO, it)
                val message = messageEntity.toString()
                server.sendMessage(message)
                LogUtil.detail(message)
            }, {
                LogUtil.detail(it.message)
            })
    }

    fun clearObserve() {
        compositeDisposables.dispose()
    }
}