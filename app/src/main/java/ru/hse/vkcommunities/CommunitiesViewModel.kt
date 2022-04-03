package ru.hse.vkcommunities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vk.dto.common.id.UserId

class CommunitiesViewModel : ViewModel() {
    private val chosenCommunities = mutableSetOf<UserId>()
    private var _numberOfChosen = MutableLiveData(0)

    val numberOfChosen: LiveData<Int> = _numberOfChosen

    var allCommunities: List<Community>? = null

    fun addChoice(id: UserId) {
        chosenCommunities.add(id)
        _numberOfChosen.value?.let {
            _numberOfChosen.value = it + 1
        }
    }

    fun removeChoice(id: UserId) {
        chosenCommunities.remove(id)
        _numberOfChosen.value?.let {
            _numberOfChosen.value = it - 1
        }
    }

    fun clearChoice() {
        chosenCommunities.clear()
        _numberOfChosen.value = 0
    }

    fun getChosenCommunities(): Iterable<UserId> = chosenCommunities
}
