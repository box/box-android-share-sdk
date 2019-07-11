package com.box.androidsdk.share.sharerepo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.models.BoxItem
import com.box.androidsdk.content.requests.BoxRequestsFile
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.*

import org.junit.*
import org.junit.rules.TestRule
import java.util.*

class ShareRepoTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    private val shareController: ShareController = mock()

    private val mockShareItem: BoxCollaborationItem = mock()
    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"

    private val canDownload = false;

    private val mockGetInviteeResponseTask: BoxFutureTask<BoxIteratorInvitees> = mock()
    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponseTask: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponseTask: BoxFutureTask<BoxResponseBatch> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockSharedLinksOperationResponseTask: BoxFutureTask<BoxItem> = mock()
    private val mockSharedLinksOperationResponse: BoxResponse<BoxItem> = mock()

    private val mockDate: Date = mock()

    private lateinit var shareRepo: ShareRepo

    @Before
    fun setup() {
        whenever(shareController.getInvitees(mockShareItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockShareItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockShareItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)
        whenever(shareController.createDefaultSharedLink(mockShareItem)).thenReturn(mockSharedLinksOperationResponseTask)
        whenever(shareController.disableShareLink(mockShareItem)).thenReturn(mockSharedLinksOperationResponseTask)
        createStubs()
    }

    /**
     * Mock callback responses.
     */
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

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxItem>
            callback.onCompleted(mockSharedLinksOperationResponse)
            null
        }.whenever(mockSharedLinksOperationResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxItem>>())
    }


    @Test
    fun `test fetch roles update LiveData values correctly`() {
        assertNull(shareRepo.getRoleItem().value) //initially the LiveData should not have any value
        shareRepo.fetchRolesFromRemote(mockShareItem) //get a value and update as needed
        assertEquals(mockFetchRolesResponse, shareRepo.getRoleItem().value)
    }


    @Test
    fun `test get invitees update LiveData values correctly`() {
        assertNull(shareRepo.getInvitees().value) //initially the LiveData should not have any value
        shareRepo.fetchInviteesFromRemote(mockShareItem, mockFilter) //get a value and update as needed
        assertEquals(mockGetInviteeResponse, shareRepo.getInvitees().value)
    }

    @Test
    fun `test add collabs update LiveData values correctly` () {
        assertNull(shareRepo.getInviteCollabsBatchResponse().value) //initially the LiveData should not have any value
        shareRepo.inviteCollabs(mockShareItem, mockSelectedRole, mockEmailList) //get a value and update as needed
        assertEquals(mockAddCollabsResponse, shareRepo.getInviteCollabsBatchResponse().value)
    }

}