package ru.hse.vkcommunities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.dto.common.id.UserId
import ru.hse.vkcommunities.model.entity.Community

class CommunitiesViewModel : ViewModel() {
    private val chosenCommunities = mutableMapOf<UserId, Community>()
    private var _numberOfChosen = MutableLiveData(0)

    val numberOfChosen: LiveData<Int> = _numberOfChosen

    var allCommunities: List<Community>? = null

    var isInSubscribeMode = false

    fun addChoice(community: Community) {
        chosenCommunities[community.id] = community
        _numberOfChosen.value?.let {
            _numberOfChosen.value = it + 1
        }
    }

    fun removeChoice(community: Community) {
        chosenCommunities.remove(community.id)
        _numberOfChosen.value?.let {
            _numberOfChosen.value = it - 1
        }
    }

    fun clearChosen() {
        chosenCommunities.values.forEach { it.isChosen = false }
        chosenCommunities.clear()
        _numberOfChosen.value = 0
    }

    fun getChosenCommunities(): Iterable<Community> = chosenCommunities.values
}
