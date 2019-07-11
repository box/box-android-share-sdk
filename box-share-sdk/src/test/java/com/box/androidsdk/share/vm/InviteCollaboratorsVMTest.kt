package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.box.androidsdk.share.utils.ShareSDKTransformer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InviteCollaboratorsVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockShareItem: BoxCollaborationItem = mock()


    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockInviteCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockGetInviteeTransformedResponse: PresenterData<BoxIteratorInvitees> = mock()
    private val mockFetchRolesTransformedResponse: PresenterData<BoxCollaborationItem> = mock()
    private val mockInviteCollabsTransformedResponse: InviteCollaboratorsPresenterData = mock()

    private val mockShareRepo: ShareRepo = mock()

    private lateinit var inviteCollabVM: InviteCollaboratorsShareVM
    private val mockTransformer: ShareSDKTransformer = mock()


    @Before
    fun setup() {
        mockShareRepo()
        inviteCollabVM = InviteCollaboratorsShareVM(mockShareRepo, mockShareItem, mockTransformer)

        attachObservers()
        mockTransformers()

    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.roleItem).thenReturn(MutableLiveData())
        whenever(mockShareRepo.inviteCollabsBatchResponse).thenReturn(MutableLiveData())
        whenever(mockShareRepo.invitees).thenReturn(MutableLiveData())

        whenever(mockShareRepo.fetchRolesFromRemote(mockShareItem)).then {
            val data = mockShareRepo.roleItem as MutableLiveData
            data.postValue(mockFetchRolesResponse)
        }

        whenever(mockShareRepo.inviteCollabs(mockShareItem, mockSelectedRole, mockEmailList)).then {
            val data = mockShareRepo.inviteCollabsBatchResponse as MutableLiveData
            data.postValue(mockInviteCollabsResponse)
        }

        whenever(mockShareRepo.fetchInviteesFromRemote(mockShareItem, mockFilter)).then {
            val data = mockShareRepo.invitees as MutableLiveData
            data.postValue(mockGetInviteeResponse)
        }
    }
    private fun mockTransformers() {
        whenever(mockTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)).thenReturn(mockFetchRolesTransformedResponse)
        whenever(mockTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)).thenReturn(mockInviteCollabsTransformedResponse)
        whenever(mockTransformer.getInviteesPresenterData(mockGetInviteeResponse)).thenReturn(mockGetInviteeTransformedResponse)
    }

    private fun attachObservers() {
        inviteCollabVM.roleItem.observeForever(mock())
        inviteCollabVM.invitees.observeForever(mock())
        inviteCollabVM.inviteCollabs.observeForever(mock())
    }

    @Test
    fun `test get fetch role item transforms to presenter data on live data change`() {
        assertNull(inviteCollabVM.roleItem.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchRolesFromRemote(mockShareItem)

        assertEquals(inviteCollabVM.roleItem.value, mockFetchRolesTransformedResponse)
    }

    @Test
    fun `test get invite collabs transforms to presenter data on live data change`() {
        assertNull(inviteCollabVM.inviteCollabs.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.inviteCollabs(mockShareItem, mockSelectedRole,mockEmailList)

        assertEquals(inviteCollabVM.inviteCollabs.value, mockInviteCollabsTransformedResponse)
    }

    @Test
    fun `test get invitees transforms to presenter data on live data change`() {
        assertNull(inviteCollabVM.invitees.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchInviteesFromRemote(mockShareItem, mockFilter)

        assertEquals(inviteCollabVM.invitees.value, mockGetInviteeTransformedResponse)
    }

}