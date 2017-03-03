package com.scopeexperts.scopemarket.helpers

import android.view.Gravity.apply
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoLogger
import seaskyways.dynamicadapter.*

/**
 * Created by User1 on 25/02 - Feb/17.
 */
class AnkoDynamicAdapter<T, A : AnkoComponent<ViewGroup>> private constructor() : DynamicAdapter<T, AnkoDynamicAdapter.AnkoViewHolder<A>?>(), AnkoLogger {
    
    companion object {
        fun <T, A : AnkoComponent<ViewGroup>> with(ankoComponentGenerator: () -> A) = AnkoDynamicAdapter<T, A>().apply {
            ankoGenerator = ankoComponentGenerator
        }
    }
    
    class AnkoViewHolder<out A>(itemView: View, val ankoComponent: A) : DynamicViewHolder(itemView)
    
    var ankoGenerator: (() -> A)? = null
    var _onBind: (AnkoDynamicAdapter<T, A>.(T, A, DynamicViewHolder, Int) -> Unit)? = null
    
    fun onAnkoBind(__onBind: AnkoDynamicAdapter<T, A>.(T, A, DynamicViewHolder, Int) -> Unit) = apply { _onBind = __onBind }
    
    fun onBindSimple(__onBindSimple: AnkoDynamicAdapter<T, A>.(T, A, Int) -> Unit) = apply {
        _onBind = { T, A, u1, position ->
            __onBindSimple(T, A, position)
        }
    }
    
    override fun data(list: Collection<T>): AnkoDynamicAdapter<T, A> {
        super.data(list)
        return this
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnkoViewHolder<A>? {
        ankoGenerator?.let { ankoGenerator ->
            val anko = ankoGenerator()
            val holder = AnkoViewHolder(anko.createView(AnkoContext.Companion.create(parent.context, parent)), anko)
            return holder
        }
        throw NullPointerException("Anko generator isn't set")
    }
    
    override fun onBindViewHolder(holder: DynamicViewHolder?, position: Int) {
        val data = dataList?.elementAtOrNull(position)
        @Suppress("UNCHECKED_CAST")
        val aholder = holder as AnkoDynamicAdapter.AnkoViewHolder<A>
        if (data != null)
            _onBind?.invoke(this, data, aholder.ankoComponent, aholder, position)
    }
}