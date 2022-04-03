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
        title = "Сообщества"

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
            adapter.setCommunitiesList(cachedCommunities)
        } else {
            VK.execute(GroupsService().groupsGetExtended(), object :
                VKApiCallback<GroupsGetObjectExtendedResponse> {
                override fun success(result: GroupsGetObjectExtendedResponse) {
                    require(Looper.myLooper() == Looper.getMainLooper())
                    val newCommunities = result.items.map { Community(it.id, it.name, it.photo200) }
                    adapter.setCommunitiesList(newCommunities)
                    viewModel.allCommunities = newCommunities
                }

                override fun fail(error: Exception) {
                    Log.e("DANIL", error.toString())
                }
            })
        }

        viewModel.numberOfChosen.observe(this) { numberOfChosen ->
            if (numberOfChosen == 0) {
                binding.actionButton.visibility = View.GONE
            } else {
                binding.actionButton.visibility = View.VISIBLE
                binding.counter.text = numberOfChosen.toString()
            }
        }

        binding.actionButton.setOnClickListener {
            val chosen = viewModel.getChosenCommunities()
            chosen.forEach {
                VK.execute(GroupsService().groupsLeave(it), object :
                    VKApiCallback<BaseOkResponse> {
                    override fun success(result: BaseOkResponse) {
                        Log.d("DANIL", "Leaved ${it.value}")
                    }

                    override fun fail(error: Exception) {
                        Log.e("DANIL", error.toString())
                    }
                })
            }
            viewModel.allCommunities?.let { old ->
                val new = old.filter { it.id !in chosen }
                viewModel.allCommunities = new
                adapter.setCommunitiesList(new)
            }
            viewModel.clearChoice()
        }
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, CommunitiesActivity::class.java)
            context.startActivity(intent)
        }
    }
}
