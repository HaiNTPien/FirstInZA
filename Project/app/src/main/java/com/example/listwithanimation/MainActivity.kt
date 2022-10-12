package com.example.listwithanimation


import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listwithanimation.adapters.ItemModel
import com.example.listwithanimation.adapters.MainRecyclerAdapter
import com.example.listwithanimation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val adapter : MainRecyclerAdapter by lazy {
        MainRecyclerAdapter()
    }
    private var inRemoveMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initList()
        initFAB()
        initItemBehavior()
    }

    private fun initItemBehavior() {
        binding.btnCancel.setOnClickListener {
            binding.cvBottomAddItem.isVisible = false
        }
        binding.btnOk.setOnClickListener {
            binding.cvBottomAddItem.isVisible = false
            addOne(
                ItemModel(
                    1,
                    null,
                    binding.edtAddName.text.toString(),
                    binding.edtAddDescription.text.toString(),
                    type = 1
                )
            )
        }
        binding.btnCancelMove.setOnClickListener {
            binding.cvBottomMoveItem.isVisible = false
        }
        binding.btnOkMove.setOnClickListener {
            binding.cvBottomMoveItem.isVisible = false
            move(binding.edtMovePosition.text.toString().toInt(), 1)
        }
    }

    private fun initFAB() {
        var count = 0
        val showFab: Animation = AnimationUtils.loadAnimation(application, R.anim.show_fab)
        val hideFab: Animation = AnimationUtils.loadAnimation(application, R.anim.hide_fab)
        binding.fab.apply {
            setOnClickListener {
                if (count % 2 == 0) {
                    binding.fabLayout.fabAdd.startAnimation(showFab)
                    binding.fabLayout.fabAdd.isClickable = true
                    binding.fabLayout.fabAdd.setOnClickListener {
                        binding.cvBottomAddItem.isVisible = true
                    }
                    binding.fabLayout.fabRemove.startAnimation(showFab)
                    binding.fabLayout.fabRemove.isClickable = true
                    binding.fabLayout.fabRemove.setOnClickListener {
                        inRemoveMode = true
                        Toast.makeText(
                            this@MainActivity,
                            "Select item to remove",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.fabLayout.fabMove.startAnimation(showFab)
                    binding.fabLayout.fabMove.isClickable = true
                    binding.fabLayout.fabMove.setOnClickListener {
                        binding.cvBottomMoveItem.isVisible = true
                    }
                } else {
                    inRemoveMode = false
                    binding.fabLayout.fabAdd.startAnimation(hideFab)
                    binding.fabLayout.fabRemove.startAnimation(hideFab)
                    binding.fabLayout.fabMove.startAnimation(hideFab)
                }
                count++
            }
        }
    }

    private fun initList() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        
        adapter.addAll(mockData())
        adapter.onItemClickCallback = {
            if (inRemoveMode && it != -1) {
                removeOne(it)
            }
        }

        val animator = SlideInDownAnimator()
        binding.rvMain.apply {
            layoutManager = linearLayoutManager
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(3, 0)
            adapter = this@MainActivity.adapter
            itemAnimator = animator
        }
    }


    private fun mockData(): List<ItemModel> {
        return listOf(
            ItemModel(1, "Group A", null, null, 0),
            ItemModel(2, null, "Item 1", "Description For Item 1", 1),
            ItemModel(3, null, "Item 2", "Description For Item 2", 1),
            ItemModel(4, null, "Item 3", "Description For Item 3", 1),
            ItemModel(4, null, "Item 4", "Description For Item 3", 1),
            ItemModel(3, null, "Item 5", "Description For Item 2", 1),
            ItemModel(4, null, "Item 6", "Description For Item 3", 1),
            ItemModel(4, null, "Item 7", "Description For Item 3", 1),
            ItemModel(3, null, "Item 8", "Description For Item 2", 1),
            ItemModel(4, null, "Item 9", "Description For Item 3", 1),
            ItemModel(4, null, "Item 10", "Description For Item 3", 1),
            ItemModel(3, null, "Item 11", "Description For Item 2", 1),
            ItemModel(4, null, "Item 12", "Description For Item 3", 1),
            ItemModel(4, null, "Item 13", "Description For Item 3", 1),
            ItemModel(3, null, "Item 14", "Description For Item 2", 1),
            ItemModel(4, null, "Item 15", "Description For Item 3", 1),
            ItemModel(4, null, "Item 16", "Description For Item 3", 1),
            ItemModel(4, null, "Item 17", "Description For Item 3", 1),
            ItemModel(4, null, "Item 18", "Description For Item 3", 1),
            ItemModel(4, null, "Item 19", "Description For Item 3", 1),
            ItemModel(4, null, "Item 20", "Description For Item 3", 1),
            ItemModel(5, "Group B", null, null, 0),
            ItemModel(6, null, "Item 21", "Description For Item Z", 1),
            ItemModel(3, null, "Item 22", "Description For Item 2", 1),
            ItemModel(4, null, "Item 23", "Description For Item 3", 1),
            ItemModel(4, null, "Item 24", "Description For Item 3", 1),
            ItemModel(4, null, "Item 25", "Description For Item 3", 1),
            ItemModel(4, null, "Item 26", "Description For Item 3", 1),
            ItemModel(4, null, "Item 27", "Description For Item 3", 1),
            ItemModel(3, null, "Item 28", "Description For Item 2", 1),
            ItemModel(4, null, "Item 29", "Description For Item 3", 1, isPivot = true)
        )
    }



    private fun move(firstPosition: Int, secondPosition: Int) {
        binding.rvMain.post {
            val item = adapter.getCurrentDataSet()[firstPosition]
            item.name = "Changed Name"
            adapter.getCurrentDataSet().removeAt(firstPosition)
            adapter.getCurrentDataSet().add(secondPosition, item)
            adapter.notifyItemMoved(firstPosition, secondPosition)
            adapter.notifyItemChanged(secondPosition)
        }
    }

    private fun removeOne(position: Int) {
        binding.rvMain.post {
            adapter.apply {
                getCurrentDataSet().removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    private fun addOne(item: ItemModel) {
        binding.rvMain.post {
            adapter.getCurrentDataSet().add(1, item)
            adapter.notifyItemInserted(1)
        }
    }
}