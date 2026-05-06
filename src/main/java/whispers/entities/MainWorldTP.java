package whispers.entities;

import cinnamon.model.ModelManager;
import cinnamon.render.model.ModelRenderer;
import cinnamon.utils.Resource;
import cinnamon.world.entity.misc.TriggerArea;

import java.util.UUID;

public class MainWorldTP extends TriggerArea {

    private final ModelRenderer model;

    public MainWorldTP() {
        super(UUID.randomUUID(), 3f, 2f, 3f);
        this.model = ModelManager.load(new Resource("whispers", "models/pumpkin/model.obj"));

        addRenderFeature((entity, camera, matrices, delta) -> {
            matrices.pushMatrix();
            matrices.translate(getPos(delta));
            model.render(matrices);
            matrices.popMatrix();
        });
    }
}
