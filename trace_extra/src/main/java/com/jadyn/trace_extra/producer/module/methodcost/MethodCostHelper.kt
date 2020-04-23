package com.jadyn.trace_extra.producer.module.methodcost

import com.jadyn.trace_extra.TraceD
import com.jadyn.trace_extra.producer.DataProducer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MethodCostHelper {

    companion object {

        fun startMethodCost() {
            TraceD.startCollectMethodCost()
        }


        fun endMethodCost() {
            val methodCostInfo = TraceD.endCollectMethodCost()
            methodCostInfo?.let {
                Observable.create<List<MethodInfo>> {
                    it.onNext(methodCostInfo)
                    it.onComplete()

                }.subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        DataProducer.producerMethodCostInfo(it)
                    }, {
                        it.printStackTrace()
                    })
            }
        }
    }
}