package com.box.androidsdk.share.sharerepo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.vm.InviteCollaboratorsDataWrapper
import com.box.androidsdk.share.vm.InviteCollaboratorsVM
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InviteCollaboratorsVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    private val shareController: ShareController = mock()

    private val mockBoxCollaborationItem: BoxCollaborationItem = mock()
    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockmShareItem: BoxCollaborationItem = mock()

    private val mockGetInviteeResponseTask: BoxFutureTask<BoxIteratorInvitees> = mock()
    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponseTask: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponseTask: BoxFutureTask<BoxResponseBatch> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private lateinit var shareRepo: ShareRepo

    private lateinit var inviteCollabVM: InviteCollaboratorsVM

    @Before
    fun setup() {

        whenever(shareController.getInvitees(mockmShareItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockmShareItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockmShareItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)

        shareRepo = ShareRepo(shareController)
        inviteCollabVM = InviteCollaboratorsVM(shareRepo, mockmShareItem)

        createStubs()
    }

    private fun createStubs() {
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>
            callback.onCompleted(mockGetInviteeResponse)
            null
        }.whenever(mockGetInviteeResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>>())
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxCollaborationItem>
            callback.onCompleted(mockFetchRolesResponse)
            null
        }.whenever(mockFetchRolesResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxCollaborationItem>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxResponseBatch>
            callback.onCompleted(mockAddCollabsResponse)
            null
        }.whenever(mockAddCollabsResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxResponseBatch>>())
    }



    @Test
    fun `test fetch role item data update success`() {
        //values are initially null
        assertNull(shareRepo.getmFetchRoleItem().value)
        assertNull(inviteCollabVM.getmFetchRoleItem().value)

        whenever(mockFetchRolesResponse.isSuccess).thenReturn(true)
        whenever(mockFetchRolesResponse.result).thenReturn(mockBoxCollaborationItem)
        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockmShareItem)

        //ShareRepo reacts by updating its LiveData correctly
        assertEquals(shareRepo.getmFetchRoleItem().value, mockFetchRolesResponse)
    }
}