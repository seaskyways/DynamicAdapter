package seaskyways.dynamicadapter

import android.support.v7.widget.RecyclerView
import android.view.*
import org.jetbrains.anko.*
import java.lang.ref.WeakReference

/**
 * Created by Ahmad on 10/11 Nov/2016.
 */
open class DynamicAdapter<T, HOLDER : DynamicViewHolder?> protected constructor() : RecyclerView.Adapter<DynamicViewHolder?>() {
    
    companion object {
        fun <T, HOLDER : DynamicViewHolder> build() = DynamicAdapter<T, HOLDER>()
        
        fun <T> buildDynamic() = DynamicAdapter<T, DynamicViewHolder?>()
    }
    
    
    protected var dataList: Collection<T>? = null
    protected var layoutInt: Int? = null
    protected var layoutView: AnkoComponent<ViewGroup>? = null
    
    protected var recyclerViewRef = WeakReference<RecyclerView?>(null)
//    private var recyclerView: RecyclerView? = null
    
    protected var onBindLambda: DynamicAdapter<T, HOLDER>.(T?, View?, DynamicViewHolder?, Int) -> Unit = { _, _, _, _ -> }
    protected var onCreateViewHolderLambda: ((parent: View, viewType: Int) -> HOLDER)? = null
    
    fun overrideViewHolder(onViewHolder: (parent: View, viewType: Int) -> HOLDER): DynamicAdapter<T, HOLDER> {
        onCreateViewHolderLambda = onViewHolder
        return this
    }
    
    open fun data(list: Collection<T>): DynamicAdapter<T, HOLDER> = apply {
        dataList = list
    }
    
    
    fun layout(layoutRes: Int? = null, view: AnkoComponent<ViewGroup>? = null): DynamicAdapter<T, HOLDER>  = apply {
        when {
            layoutRes != null -> layoutInt = layoutRes
            view != null -> layoutView = view
            else -> throw NullPointerException()
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    open fun onBind(onBind: DynamicAdapter<T, HOLDER>.(data: T, view: View?, holder: HOLDER, position: Int) -> Unit): DynamicAdapter<T, HOLDER> {
        onBindLambda = onBind as DynamicAdapter<T, HOLDER>.(T?, View?, DynamicViewHolder?, Int) -> Unit
        return this
    }
    
    fun into(recycler: RecyclerView, layoutManager: RecyclerView.LayoutManager): RecyclerView.Adapter<DynamicViewHolder?> {
        recyclerViewRef = WeakReference(recycler)
        recycler.adapter = this
        recycler.layoutManager = layoutManager
        return this
    }
    
    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }
    
    override fun onBindViewHolder(holder: DynamicViewHolder?, position: Int) {
        onBindLambda(dataList?.elementAt(position), holder?.itemView, holder, position)
    }
    
    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DynamicViewHolder? {
        if (onCreateViewHolderLambda != null) {
            return onCreateViewHolderLambda?.invoke(layoutView!!.createView(AnkoContext.Companion.create(parent.context, parent)), viewType)!!
        } else {
            when {
                layoutInt != null -> return BasicViewHolder(parent.context.layoutInflater.inflate(layoutInt!!, parent, false))
                layoutView != null -> return BasicViewHolder(layoutView!!.createView(AnkoContext.Companion.create(parent.context, parent)))
            }
        }
        return null
    }
    
    inner class BasicViewHolder(itemView: View?) : DynamicViewHolder(itemView)
}

abstract class DynamicViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)

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
        _onBind = { T, A, _, position ->
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