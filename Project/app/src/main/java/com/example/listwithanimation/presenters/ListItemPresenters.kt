package com.example.listwithanimation.presenters

import com.example.listwithanimation.`interface`.ListItemView
import com.example.listwithanimation.models.ItemModel


class ListItemPresenters(
    private var mainView: ListItemView.View?,
    private val model: ListItemView.Model): ListItemView.Presenter {
    override fun onShuffleButtonClick() {
        mainView?.setList(model.generateList(1000))
    }

    override fun onAddButtonClick(item: ItemModel) {
        mainView?.addOne(item)
    }

    override fun onRemoveButtonClick(position: Int) {
        mainView?.removeOne(position)
    }

    override fun onMoveButtonClick(startPosition: Int, endPosition: Int) {
        mainView?.move(startPosition, endPosition)
    }

}
