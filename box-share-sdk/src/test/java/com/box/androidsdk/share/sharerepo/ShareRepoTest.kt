package com.box.androidsdk.share.sharerepo

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.*

import org.junit.*
import org.junit.rules.TestRule

class ShareRepoTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    private val shareController: ShareController = mock()

    private val mockBoxCollaborationItem: BoxCollaborationItem = mock()
    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR

    private var mockGetInviteeResponseTask: BoxFutureTask<BoxIteratorInvitees> = mock()
    private var mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponseTask: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponseTask: BoxFutureTask<BoxResponseBatch> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()
    private val mockFilter: String = "filter"
    private lateinit var shareRepo: ShareRepo

    @Before
    fun setup() {
        whenever(shareController.getInvitees(mockBoxCollaborationItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockBoxCollaborationItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockBoxCollaborationItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)
        shareRepo = ShareRepo(shareController)
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
    fun `test fetch roles update LiveData values correctly`() {
        assertNull(shareRepo.getmFetchRoleItem().value) //initially the LiveData should not have any value
        shareRepo.fetchRolesApi(mockBoxCollaborationItem) //get a value and update as needed
        assertEquals(mockFetchRolesResponse, shareRepo.getmFetchRoleItem().value)
    }


    @Test
    fun `test get invitees update LiveData values correctly`() {
        assertNull(shareRepo.getmInvitees().value) //initially the LiveData should not have any value
        shareRepo.getInviteesApi(mockBoxCollaborationItem, mockFilter) //get a value and update as needed
        assertEquals(mockGetInviteeResponse, shareRepo.getmInvitees().value)
    }

    @Test
    fun `test get collab update LiveData values correctly` () {
        assertNull(shareRepo.getmInviteCollabBatch().value) //initially the LiveData should not have any value
        shareRepo.addCollabsApi(mockBoxCollaborationItem, mockSelectedRole, mockEmailList) //get a value and update as needed
        assertEquals(mockAddCollabsResponse, shareRepo.getmInviteCollabBatch().value)
    }

}