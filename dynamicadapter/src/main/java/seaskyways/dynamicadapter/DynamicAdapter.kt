package seaskyways.dynamicadapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.layoutInflater
import java.lang.ref.WeakReference

/**
 * Created by Ahmad on 10/11 Nov/2016.
 */
open class DynamicAdapter<T, HOLDER : DynamicViewHolder?> : RecyclerView.Adapter<DynamicViewHolder?>() {
    
    companion object {
        fun <T, HOLDER : DynamicViewHolder> build() = DynamicAdapter<T, HOLDER>()
        
        fun <T> buildDynamic() = DynamicAdapter<T, DynamicViewHolder?>()
    }
    
    
    protected var dataList: Collection<T>? = null
    protected var layoutInt: Int? = null
    protected var layoutView: AnkoComponent<ViewGroup>? = null
    
    protected var recyclerViewRef = WeakReference<RecyclerView?>(null)
//    private var recyclerView: RecyclerView? = null
    
    protected var onBindLambda: DynamicAdapter<T, HOLDER>.(T?, View?, DynamicViewHolder?, Int) -> Unit = { u1, u2, u3, u4 -> }
    protected var onCreateViewHolderLambda: ((parent: View, viewType: Int) -> HOLDER)? = null
    
    fun overrideViewHolder(onViewHolder: (parent: View, viewType: Int) -> HOLDER): DynamicAdapter<T, HOLDER> {
        onCreateViewHolderLambda = onViewHolder
        return this
    }
    
    open fun data(list: Collection<T>): DynamicAdapter<T, HOLDER> = apply {
        dataList = list
    }
    
    
    fun layout(layoutRes: Int? = null, view: AnkoComponent<ViewGroup>? = null): DynamicAdapter<T, HOLDER> = apply {
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