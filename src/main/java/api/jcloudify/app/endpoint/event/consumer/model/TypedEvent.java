package api.jcloudify.app.endpoint.event.consumer.model;

import api.jcloudify.app.PojaGenerated;
import api.jcloudify.app.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
