package whispers.entities;

import cinnamon.math.Rotation;
import cinnamon.model.ModelManager;
import cinnamon.render.model.ModelRenderer;
import cinnamon.utils.Resource;
import cinnamon.world.entity.misc.TriggerArea;

import java.util.UUID;

public class CemeteryTP extends TriggerArea {

    private final ModelRenderer model;

    public CemeteryTP() {
        super(UUID.randomUUID(), 3f, 2f, 3f);
        this.model = ModelManager.load(new Resource("whispers", "models/tombstone/model.obj"));

        addRenderFeature((entity, camera, matrices, delta) -> {
            if (!getWorld().isNight())
                return;

            matrices.pushMatrix();
            matrices.translate(getPos(delta));
            matrices.rotate(Rotation.Z.rotationDeg(-22.5f));
            model.render(matrices);
            matrices.popMatrix();
        });
    }
}
