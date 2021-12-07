package ch.post.strm.poc.dataflows.service

import ch.post.strm.poc.dataflows.service.model.Application
import org.springframework.stereotype.Service

@Service
class ApplicationService {

    fun find(): Map<String, Application> {
        return mapOf(
            "PADASA" to Application("PADASA"),
            "FISA" to Application("FISA"),
            "EKP" to Application("EKP"),
            "TT" to Application("TT")
        )
    }

}