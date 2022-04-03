package ru.hse.vkcommunities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import ru.hse.vkcommunities.databinding.ActivityCommunitiesBinding

class CommunitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunitiesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val columns = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            Configuration.ORIENTATION_LANDSCAPE -> 5
            else -> 3
        }
        binding.communities.layoutManager =
            GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false)
        val adapter = CommunitiesAdapter(getColor(R.color.light_blue))
        binding.communities.adapter = adapter

        VK.execute(GroupsService().groupsGetExtended(), object :
            VKApiCallback<GroupsGetObjectExtendedResponse> {
            override fun success(result: GroupsGetObjectExtendedResponse) {
                require(Looper.myLooper() == Looper.getMainLooper())
                val newCommunities = result.items.map { Community(it.name, it.photo200) }
                adapter.setCommunitiesList(newCommunities)
            }

            override fun fail(error: Exception) {
                Log.e("DANIL", error.toString())
            }
        })
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, CommunitiesActivity::class.java)
            context.startActivity(intent)
        }
    }
}
