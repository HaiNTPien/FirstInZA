package com.example.listwithanimation.models

class ListItemModel {
     fun generateList(size: Int): List<ItemModel> {
            val list = mutableListOf<ItemModel>()
            for(i in 0..size) {
                list.add(ItemModel(i + 1, null, "Item " + (i + 1).toString(), "Description for " + (i + 1).toString(), type = 1))
            }
            list[size].isPivot = true
            return list.toList()

    }
}

data class ItemModel(
    var id: Int,
    var title: String? = null,
    var name: String? = null,
    var description: String? = null,
    var type: Int = 0,
    var isPivot: Boolean = false
)