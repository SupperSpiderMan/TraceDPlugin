package com.jadyn.trace_extra.producer

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

open class BaseProducer<T> {

    private var mSubject: Subject<T> = PublishSubject.create()


    fun produce(data: T) {
        mSubject.onNext(data)
    }

    fun subject(): Observable<T> {
        return mSubject
    }

}