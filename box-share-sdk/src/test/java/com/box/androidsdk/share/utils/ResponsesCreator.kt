package com.box.androidsdk.share.utils

import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class ResponsesCreator {
    companion object {
        fun createFailedBoxCollaborationResponse(dummyName: String, exception: Exception): BoxResponse<BoxCollaboration> {
            val boxResponse: BoxResponse<BoxCollaboration> = mock()
            whenever(boxResponse.exception).thenReturn(exception)
            val boxUser: BoxUser = mock()

            whenever(boxUser.login).thenReturn(dummyName)
            val boxRequestShare: BoxRequestsShare.AddCollaboration = mock()
            whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
            whenever(boxResponse.request).thenReturn(boxRequestShare)
            whenever(boxResponse.isSuccess).thenReturn(false)
            return boxResponse
        }

        fun createFailedBoxIterCollaborationResponse(exception: Exception): BoxResponse<BoxIteratorCollaborations> {
            val boxResponse: BoxResponse<BoxIteratorCollaborations> = mock()
            whenever(boxResponse.exception).thenReturn(exception)
            whenever(boxResponse.isSuccess).thenReturn(false)
            return boxResponse
        }


        fun createSuccessfulBoxCollaborationResponse(dummyName: String?): BoxResponse<BoxCollaboration> {
            val boxResponse: BoxResponse<BoxCollaboration> = mock()
            var boxUser: BoxUser? = null
            if (dummyName != null) {
                boxUser = mock()
                whenever(boxUser.login).thenReturn(dummyName)
            }
            val boxRequestShare: BoxCollaboration = mock()
            whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
            whenever(boxResponse.result).thenReturn(boxRequestShare)
            whenever(boxResponse.isSuccess).thenReturn(true)
            return boxResponse
        }

        fun createSuccessfulBoxItemResponse(): BoxResponse<BoxItem> {
            val boxResponse: BoxResponse<BoxItem> = mock()
            whenever(boxResponse.isSuccess).thenReturn(true)
            whenever(boxResponse.result).thenReturn(mock())
            whenever(boxResponse.request).thenReturn(mock())
            return boxResponse
        }


        fun createFailedBoxItemResponse(e: Exception): BoxResponse<BoxItem> {
            val boxResponse: BoxResponse<BoxItem> = mock()
            whenever(boxResponse.isSuccess).thenReturn(false)
            whenever(boxResponse.exception).thenReturn(e)
            return boxResponse
        }

        fun createBoxCollaborationResponseBatch(vararg respones: BoxResponse<BoxCollaboration>): BoxResponseBatch {
            val boxResponses: BoxResponseBatch = mock()
            val boxResponseList = arrayListOf<BoxResponse<BoxObject>>()
            respones.forEach { boxResponse -> boxResponseList.add(boxResponse as BoxResponse<BoxObject>) }
            whenever(boxResponses.responses).thenReturn(boxResponseList)
            return boxResponses
        }
    }
}
