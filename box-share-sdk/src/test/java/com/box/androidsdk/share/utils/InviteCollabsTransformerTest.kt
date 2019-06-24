package com.box.androidsdk.share.utils;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.models.BoxError
import com.box.androidsdk.content.requests.BoxRequestsShare
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.share.R
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createBoxResponseBatch
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createSuccessResponse
import com.box.androidsdk.share.vm.InviteCollaboratorsShareVM
import com.box.androidsdk.share.vm.PresenterData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test;
import org.junit.rules.TestRule
import javax.net.ssl.HttpsURLConnection

class InviteCollabsTransformerTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun `test get invite collabs presenter data for successful request where response size is 1 and accessible by is not null`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborator_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for successful request where response size is 1 and accessible by is null`() {
        //configs
        val dummyName = null
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)


        //compare values
        assertEquals(R.string.box_sharesdk_collaborators_invited, result.strCode)
        assertEquals(null, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for successful request where response size is greater than 1`() {
        //configs
        val dummyName = "Box User"
        val boxResponse: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName)
        val boxResponse2: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName+"2")
        val boxResponses = createBoxResponseBatch(boxResponse, boxResponse2)

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForSuccessfulRequest(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborators_invited, result.strCode)
        assertEquals(null, result.data)
        assertEquals(true, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)
    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is not empty`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()
        failedCollabs.add("failure 1")
        failedCollabs.add("failure 2")
        val res = "failure 1 failure 2"


        //process request
        val result = InviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_following_collaborators_error, result.strCode)
        assertEquals(res, result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(true, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty and alreadyAddedCount is 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess) //request failing only due to adding already added collabs is still considered a success
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(1, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty alreadyAddedCount is greater than 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 2
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.plurals.box_sharesdk_already_been_invited, result.strCode)
        assertEquals(dummyName, result.data)
        assertEquals(true, result.isSuccess) //request failing only due to adding already added collabs is still considered a success
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(2, result.alreadyAdddedCount)

    }

    @Test
    fun `test get invite collabs presenter data for failed request where failedCollabsList is empty alreadyAddedCount is 0`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 0
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = InviteCollabsTransformer.getPresenterDataForFailedRequest(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_unable_to_invite, result.strCode)
        assertEquals(null, result.data)
        assertEquals(false, result.isSuccess)
        assertEquals(false, result.isSnackBarMessage)
        assertEquals(0, result.alreadyAdddedCount)

    }
}
