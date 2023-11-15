package HeldItemSaver.events;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import kotlin.Unit;

public class CatchEventListener {

    public CatchEventListener() {
        subscribeToCatchEvents();
    }

    private void subscribeToCatchEvents(){
        // Subscribe to the Catch Event
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.NORMAL, this::CatchHandler);
    }

    private Unit CatchHandler(PokemonCapturedEvent event){
        // Process the event
        onCatchEvent(event);
        return Unit.INSTANCE;
    }

    private void onCatchEvent(PokemonCapturedEvent event){


    }
}
