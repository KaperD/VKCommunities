package ru.hse.vkcommunities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.GroupsService
import com.vk.sdk.api.groups.dto.GroupsGetObjectExtendedResponse
import kotlinx.coroutines.*
import ru.hse.vkcommunities.databinding.ActivityCommunitiesBinding
import ru.hse.vkcommunities.model.entity.Community
import ru.hse.vkcommunities.model.repository.RecentCommunitiesRepository
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CommunitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunitiesBinding
    private lateinit var viewModel: CommunitiesViewModel
    private lateinit var adapter: CommunitiesAdapter
    private lateinit var recentCommunitiesRepository: RecentCommunitiesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommunitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.bringToFront()
        binding.progressBar.setOnClickListener {  }

        viewModel = ViewModelProvider(this).get(CommunitiesViewModel::class.java)
        recentCommunitiesRepository = RecentCommunitiesRepository(this)

        val columns = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 3
            Configuration.ORIENTATION_LANDSCAPE -> 5
            else -> 3
        }
        binding.communities.layoutManager =
            GridLayoutManager(this, columns, GridLayoutManager.VERTICAL, false)
        adapter = CommunitiesAdapter(viewModel, getColor(R.color.light_blue))
        binding.communities.adapter = adapter

        setupCommunities(binding.switchMode.isChecked)

        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.clearChoice()
            setupCommunities(isChecked)
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

    private fun setupCommunities(isChecked: Boolean) {
        lifecycleScope.launch {
            if (viewModel.allCommunities == null) {
                binding.progressBar.visibility = View.VISIBLE
                binding.body.visibility = View.INVISIBLE
                try {
                    viewModel.allCommunities = loadData()
                } catch (ignored: RepeatException) {
                    setupCommunities(isChecked)
                    return@launch
                } catch (ignored: Exception) {
                    binding.progressBar.visibility = View.INVISIBLE
                    return@launch
                }
                binding.progressBar.visibility = View.INVISIBLE
                binding.body.visibility = View.VISIBLE
            }
            if (isChecked) {
                setupSubscribe()
            } else {
                setupUnsubscribe()
            }
        }
    }

    private fun setupSubscribe() {
        updateSubscribeList()
        binding.actionName.text = getString(R.string.subscribe)
        binding.actionButton.setOnClickListener {
            makeActionWithSelectedCommunities({ safeJoinCommunity(it) }) {
                updateSubscribeList()
            }
        }
    }

    private suspend fun safeJoinCommunity(community: Community) {
        try {
            joinCommunity(community)
            recentCommunitiesRepository.delete(community)
            community.isSubscribed = true
        } catch (ignored: RepeatException) {
            safeJoinCommunity(community)
        } catch (ignored: Exception) {
            return
        }
    }

    private fun updateSubscribeList() {
        viewModel.allCommunities?.let { all ->
            adapter.setCommunitiesList(all.filter { !it.isSubscribed })
        }
    }

    private fun setupUnsubscribe() {
        updateUnsubscribeList()
        binding.actionName.text = getString(R.string.unsubscribe)
        binding.actionButton.setOnClickListener {
            makeActionWithSelectedCommunities({ safeLeaveCommunity(it) }) {
                updateUnsubscribeList()
            }
        }
    }

    private suspend fun safeLeaveCommunity(community: Community) {
        try {
            leaveCommunity(community)
            recentCommunitiesRepository.insert(community)
            community.isSubscribed = false
        } catch (ignored: RepeatException) {
            safeLeaveCommunity(community)
        } catch (ignored: Exception) {
            return
        }
    }

    private fun updateUnsubscribeList() {
        viewModel.allCommunities?.let { all ->
            adapter.setCommunitiesList(all.filter { it.isSubscribed })
        }
    }

    private fun makeActionWithSelectedCommunities(
        actionWithCommunity: suspend (Community) -> Unit,
        actionAfter: suspend () -> Unit
    ) {
        lifecycleScope.launch {
            binding.body.alpha = 0.5f
            binding.progressBar.visibility = View.VISIBLE
            val chosen = viewModel.getChosenCommunities()
            withContext(Dispatchers.IO) {
                chosen.forEach {
                    actionWithCommunity(it)
                }
            }
            actionAfter()
            viewModel.clearChoice()
            binding.progressBar.visibility = View.INVISIBLE
            binding.body.alpha = 1f
        }
    }

    private suspend fun loadData(): List<Community> = withContext(Dispatchers.IO) {
        val communities = mutableListOf<Community>()
        communities.addAll(recentCommunitiesRepository.getAll())
        var offset = 0
        while (true) {
            val result = loadCommunities(offset)
            if (result.items.isEmpty()) {
                break
            }
            offset += result.items.size

            val newCommunities = result.items.map {
                Community(
                    it.id,
                    it.name,
                    it.photo200,
                    isSubscribed = true
                )
            }
            communities.addAll(newCommunities)
        }
        communities
    }

    private suspend fun loadCommunities(offset: Int): GroupsGetObjectExtendedResponse =
        suspendCoroutine { continuation ->
            VK.execute(
                GroupsService().groupsGetExtended(offset = offset),
                object : VKApiCallback<GroupsGetObjectExtendedResponse> {
                    override fun success(result: GroupsGetObjectExtendedResponse) {
                        continuation.resume(result)
                    }

                    override fun fail(error: Exception) {
                        AlertDialog.Builder(this@CommunitiesActivity)
                            .setMessage("Произошла ошибка во время загрузки сообществ")
                            .setPositiveButton("Повторить") { _, _ ->
                                continuation.resumeWithException(RepeatException)
                            }
                            .setNegativeButton("Отменить") { _, _ ->
                                continuation.resumeWithException(error)
                            }
                            .show()
                    }
                }
            )
        }

    private suspend fun leaveCommunity(community: Community): BaseOkResponse =
        suspendCoroutine { continuation ->
            VK.execute(GroupsService().groupsLeave(community.id), object :
                VKApiCallback<BaseOkResponse> {
                override fun success(result: BaseOkResponse) {
                    continuation.resume(result)
                }

                override fun fail(error: Exception) {
                    AlertDialog.Builder(this@CommunitiesActivity)
                        .setMessage("Произошла ошибка во время загрузки сообществ")
                        .setPositiveButton("Повторить") { _, _ ->
                            continuation.resumeWithException(RepeatException)
                        }
                        .setNegativeButton("Пропустить") { _, _ ->
                            continuation.resumeWithException(error)
                        }
                        .show()
                }
            })
        }

    private suspend fun joinCommunity(community: Community): BaseOkResponse =
        suspendCoroutine { continuation ->
            VK.execute(GroupsService().groupsJoin(community.id), object :
                VKApiCallback<BaseOkResponse> {
                override fun success(result: BaseOkResponse) {
                    continuation.resume(result)
                }

                override fun fail(error: Exception) {
                    AlertDialog.Builder(this@CommunitiesActivity)
                        .setMessage("Произошла ошибка во время загрузки сообществ")
                        .setPositiveButton("Повторить") { _, _ ->
                            continuation.resumeWithException(RepeatException)
                        }
                        .setNegativeButton("Пропустить") { _, _ ->
                            continuation.resumeWithException(error)
                        }
                        .show()
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

object RepeatException : Exception()
