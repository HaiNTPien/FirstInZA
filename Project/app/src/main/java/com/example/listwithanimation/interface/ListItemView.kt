package com.example.listwithanimation.`interface`

import com.example.listwithanimation.models.ItemModel

interface ListItemView {
    interface View {
        fun move(firstPosition: Int, secondPosition: Int)

        fun removeOne(position: Int)

        fun addOne(item: ItemModel)

        fun setList(lst: List<ItemModel>)
    }

    interface Model {
        fun generateList(size: Int): List<ItemModel>
    }

    interface Presenter {
        fun onShuffleButtonClick()
        fun onAddButtonClick(item: ItemModel)
        fun onRemoveButtonClick(position: Int)
        fun onMoveButtonClick(startPosition: Int, endPosition: Int)
    }
}