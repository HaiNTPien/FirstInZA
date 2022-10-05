package com.example.listwithanimation

import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.listwithanimation.adapters.ItemModel
import com.example.listwithanimation.adapters.MainRecyclerAdapter
import com.example.listwithanimation.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter : MainRecyclerAdapter
    var inRemoveMode: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initList()
        initFAB()
        initAddItemBehavior()
    }

    private fun initAddItemBehavior() {
        binding.btnCancel.setOnClickListener {
            binding.cvBottomAddItem.isVisible = false
            hideKeyboard()
        }
        binding.btnOk.setOnClickListener {
            binding.cvBottomAddItem.isVisible = false
            hideKeyboard()
            adapter.addOne(ItemModel(1, null, binding.edtAddName.text.toString(), binding.edtAddDescription.text.toString(), type = 1))
            binding.rvMain.smoothScrollToPosition(0)
        }
        binding.btnCancelMove.setOnClickListener {
            binding.cvBottomMoveItem.isVisible = false
            hideKeyboard()
        }
        binding.btnOkMove.setOnClickListener {
            binding.cvBottomMoveItem.isVisible = false
            hideKeyboard()
            adapter.move(binding.edtMovePosition.text.toString().toInt(), 0)
            binding.rvMain.smoothScrollToPosition(0)
        }
    }

    private fun initFAB() {
        var count = 0
        val showFab: Animation = AnimationUtils.loadAnimation(application, R.anim.show_fab)
        val hideFab: Animation = AnimationUtils.loadAnimation(application, R.anim.hide_fab)
        binding.fab.apply {
            setOnClickListener {
                if(count % 2 == 0) {
                    binding.fabLayout.fabAdd.startAnimation(showFab)
                    binding.fabLayout.fabAdd.isClickable = true
                    binding.fabLayout.fabAdd.setOnClickListener {
                        binding.cvBottomAddItem.isVisible = true
                    }
                    binding.fabLayout.fabRemove.startAnimation(showFab)
                    binding.fabLayout.fabRemove.isClickable = true
                    binding.fabLayout.fabRemove.setOnClickListener {
                        inRemoveMode = true
                        Toast.makeText(this@MainActivity, "Select item to remove", Toast.LENGTH_SHORT).show()
                    }
                    binding.fabLayout.fabMove.startAnimation(showFab)
                    binding.fabLayout.fabMove.isClickable = true
                    binding.fabLayout.fabMove.setOnClickListener {
                        binding.cvBottomMoveItem.isVisible = true
                    }
                }else {
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
        adapter = MainRecyclerAdapter()
        adapter.setActivityContext(context = this)
        var linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//        linearLayoutManager.stackFromEnd = true
        binding.rvMain.apply {
            layoutManager = linearLayoutManager
        }
        adapter.addAll(mockData())

        adapter.onItemClickCallback = {
            if(inRemoveMode && it != -1) {
                adapter.removeOne(binding.rvMain.findViewHolderForAdapterPosition(it)?.itemView ,it)
            }
        }
        binding.rvMain.adapter = adapter
//        val animator = CustomAnimators(this)
//        binding.rvMain.itemAnimator = animator
        (binding.rvMain.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }


    fun mockData() : List<ItemModel> {
        return listOf(ItemModel(1, "Group A", null, null, 0),
            ItemModel(2, null , "Item 1", "Description For Item 1",1),
            ItemModel(3, null, "Item 2", "Description For Item 2",1),
            ItemModel(4, null, "Item 3", "Description For Item 3",1),
            ItemModel(5, "Group B", null, null,0),
            ItemModel(6, null, "Item Z", "Description For Item Z",1),
            ItemModel(7, null, "Item X", "Description For Item X",1))
    }

    private fun hideKeyboard() {
        this.currentFocus?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}