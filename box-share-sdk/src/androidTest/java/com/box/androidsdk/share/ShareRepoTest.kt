package com.box.androidsdk.share

import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertEquals

import org.junit.*

class ShareRepoTest {
    private val shareController: ShareController = mock()

    private val mockGetInviteeResponse: BoxFutureTask<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponse: BoxFutureTask<BoxResponseBatch> = mock()

    private lateinit var shareRepo: ShareRepo

    @Before
    fun setup() {
        whenever(shareController.getInvitees(any(), any())).thenReturn(mockGetInviteeResponse)
        whenever(shareController.fetchRoles(any())).thenReturn(mockFetchRolesResponse)
        whenever(shareController.addCollaborations(any(), any(), any())).thenReturn(mockAddCollabsResponse)

        shareRepo = ShareRepo(shareController)
    }

    @Test
    fun getInvitees () {
        val rolesResponse = shareRepo.fetchRoles(any())

        assertEquals(rolesResponse, mockFetchRolesResponse)
    }
}