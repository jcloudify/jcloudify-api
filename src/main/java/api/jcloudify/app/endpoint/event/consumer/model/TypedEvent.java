package api.jcloudify.app.endpoint.event.consumer.model;

import api.jcloudify.app.PojaGenerated;

@PojaGenerated
public record TypedEvent(String typeName, Object payload) {}
