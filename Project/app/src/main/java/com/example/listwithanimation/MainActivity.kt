package com.example.listwithanimation


import android.os.Bundle
import android.util.Log
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
                    binding.fabLayout.fabShuffle.startAnimation(showFab)
                    binding.fabLayout.fabShuffle.isClickable = true
                    binding.fabLayout.fabShuffle.setOnClickListener {
                        shuffleList()
                    }

                } else {
                    inRemoveMode = false
                    binding.fabLayout.fabAdd.startAnimation(hideFab)
                    binding.fabLayout.fabRemove.startAnimation(hideFab)
                    binding.fabLayout.fabMove.startAnimation(hideFab)
                    binding.fabLayout.fabShuffle.startAnimation(hideFab)
                }
                count++
            }
        }
    }

    private fun initList() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter.addAll(mockData().toMutableList().reversed().toList())
        adapter.onItemClickCallback = {
            Toast.makeText(this@MainActivity, it.toString() + " " + adapter.itemCount.toString(), Toast.LENGTH_SHORT).show()
            if (inRemoveMode && it != -1) {
                removeOne(it)
            }
        }

        binding.rvMain.apply {
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(3, 0)
            layoutManager = linearLayoutManager
            adapter = this@MainActivity.adapter
            val animator = SlideInDownAnimator()
            animator.callbackNotifyDataSetChanged = {
                binding.rvMain.post{
                    Toast.makeText(this@MainActivity, " NOTIFY ", Toast.LENGTH_SHORT).show()
                    this@MainActivity.adapter.notifyDataSetChanged()
                }
            }
            itemAnimator = animator
        }
    }


    private fun mockData(): List<ItemModel> {
        val list = mutableListOf<ItemModel>()
        for(i in 0..10000) {
            list.add(ItemModel(i + 1, null, "Item " + (i + 1).toString(), "Description for " + (i + 1).toString(), type = 1))
        }
        list[10000].isPivot = true
        return list.toList()
    }

    private fun mockData2(): List<ItemModel> {
        val list = mutableListOf<ItemModel>()
        for(i in 0..10000) {
            list.add(ItemModel(i + 1, null, "Item " + (i + 1).toString(), "Description for " + (i + 1).toString(), type = 1))
        }
        list[10000].isPivot = true
        return list.toList()
    }

    private fun move(firstPosition: Int, secondPosition: Int) {
        runOnUiThread {
            adapter.moveOne(firstPosition, secondPosition)
        }
    }

    private fun removeOne(position: Int) {
        runOnUiThread {
            adapter.removeOne(position)
        }
    }

    private fun addOne(item: ItemModel) {
        runOnUiThread {
            adapter.addOne(1, item)
        }
    }

    private fun shuffleList() {
        val layoutManager = binding.rvMain.layoutManager as LinearLayoutManager
        val list = mockData().toMutableList()
        val item = list[3]
        list[3] = list[2]
        list[2] = item
        list.removeAt(7)
        list.add(9, ItemModel(150, null, "Item " + (150).toString(), "Description for " + (150).toString(), type = 1))
        runOnUiThread {
            adapter.submitDataList(list.toList(), layoutManager.findFirstCompletelyVisibleItemPosition(), layoutManager.findLastCompletelyVisibleItemPosition())
        }
    }
}