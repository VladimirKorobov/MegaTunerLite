package com.example.megatuner.Interfaces;

import com.example.megatuner.Interfaces.TuneRenderer;

public interface TuneView {

	void invalidateRender();
	TuneRenderer getRenderer();
    void Cleanup();
}
