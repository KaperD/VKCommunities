package ru.hse.vkcommunities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import ru.hse.vkcommunities.databinding.ActivityCommunitiesBinding

class CommunitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunitiesBinding
    private lateinit var viewModel: CommunitiesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(CommunitiesViewModel::class.java)

        val columns = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            Configuration.ORIENTATION_LANDSCAPE -> 5
            else -> 3
        }
        binding.communities.layoutManager =
            GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false)
        val adapter = CommunitiesAdapter(viewModel, getColor(R.color.light_blue))
        binding.communities.adapter = adapter

        val cachedCommunities = viewModel.allCommunities
        if (cachedCommunities != null) {
            setup(binding.switchMode.isChecked, adapter)
        } else {
            VK.execute(GroupsService().groupsGetExtended(), object :
                VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    require(Looper.myLooper() == Looper.getMainLooper())
                    val newCommunities = result.items.map { Community(it.id, it.name, it.photo200) }
                    viewModel.allCommunities = newCommunities
                    setup(binding.switchMode.isChecked, adapter)
                }

                override fun fail(error: Exception) {
                    Log.e("DANIL", error.toString())
                }
            })
        }

        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            setup(isChecked, adapter)
        }

        viewModel.numberOfChosen.observe(this) { numberOfChosen ->
            if (numberOfChosen == 0) {
                binding.actionButton.visibility = View.GONE
            } else {
                binding.actionButton.visibility = View.VISIBLE
                binding.counter.text = numberOfChosen.toString()
            }
        }
    }

    private fun setup(isChecked: Boolean, adapter: CommunitiesAdapter) {
        if (isChecked) {
            setupSubscribe(adapter)
        } else {
            setupUnsubscribe(adapter)
        }
    }

    private fun setupSubscribe(adapter: CommunitiesAdapter) {
        viewModel.clearChoice()
        updateSubscribeList(adapter)
        binding.actionName.text = getString(R.string.subscribe)
        binding.actionButton.setOnClickListener {
            val chosen = viewModel.getChosenCommunities()
            chosen.forEach {
                it.isSubscribed = true
                VK.execute(GroupsService().groupsJoin(it.id), object :
                    VKApiCallback<BaseOkResponse> {
                    override fun success(result: BaseOkResponse) {
                        Log.d("DANIL", "Joined ${it.name}")
                    }

                    override fun fail(error: Exception) {
                        Log.e("DANIL", error.toString())
                    }
                })
            }
            updateSubscribeList(adapter)
            viewModel.clearChoice()
        }
    }

    private fun updateSubscribeList(adapter: CommunitiesAdapter) {
        viewModel.allCommunities?.let { all ->
            adapter.setCommunitiesList(all.filter { !it.isSubscribed })
        }
    }

    private fun setupUnsubscribe(adapter: CommunitiesAdapter) {
        viewModel.clearChoice()
        updateUnsubscribeList(adapter)
        binding.actionName.text = getString(R.string.unsubscribe)
        binding.actionButton.setOnClickListener {
            val chosen = viewModel.getChosenCommunities()
            chosen.forEach {
                it.isSubscribed = false
                VK.execute(GroupsService().groupsLeave(it.id), object :
                    VKApiCallback<BaseOkResponse> {
                    override fun success(result: BaseOkResponse) {
                        Log.d("DANIL", "Left ${it.name}")
                    }

                    override fun fail(error: Exception) {
                        Log.e("DANIL", error.toString())
                    }
                })
            }
            updateUnsubscribeList(adapter)
            viewModel.clearChoice()
        }
    }

    private fun updateUnsubscribeList(adapter: CommunitiesAdapter) {
        viewModel.allCommunities?.let { all ->
            adapter.setCommunitiesList(all.filter { it.isSubscribed })
        }
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, CommunitiesActivity::class.java)
            context.startActivity(intent)
        }
    }
}
