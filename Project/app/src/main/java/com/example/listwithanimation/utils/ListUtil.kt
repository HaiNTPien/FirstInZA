package com.example.listwithanimation.utils

import com.example.listwithanimation.models.ContactModel
import java.util.*

class ListUtil {
    companion object {
        private fun String.onlyLetters() = all { it.isLetter() }

        fun addLabelSection(lst: MutableList<ContactModel>): List<ContactModel> {
            var previousLabel = ""
            var countSpecialName = 0
            for (i in lst.size - 1 downTo 0) {
                if (!lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT).onlyLetters()) {
                    countSpecialName++
                    if (previousLabel == "#") {
                        lst.removeAt(i + 1)
                    }
                    previousLabel = "#"
                    lst.add(i, ContactModel("", "","", mutableListOf(), "", previousLabel))
                } else {
                    if (previousLabel != lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)) {
                        previousLabel = lst[i].displayName.substring(0, 1).uppercase(Locale.ROOT)
                        lst.add(i, ContactModel("", "","", mutableListOf(), "",  previousLabel))
                    } else {
                        lst.removeAt(i + 1)
                        lst.add(i, ContactModel("", "" ,"", mutableListOf(), "", previousLabel))
                    }
                }

            }
            return if (countSpecialName > 0) {
                val secondList = lst.subList(0, countSpecialName + 1)
                val firstList = lst.subList(countSpecialName + 1, lst.size)
                firstList + secondList
            } else {
                lst
            }
        }
    }
}