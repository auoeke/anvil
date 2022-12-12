package user11681.anvil.event;

import net.minecraft.util.ActionResult;
import user11681.anvil.Anvil;

public abstract class AnvilEvent {
	protected ActionResult result;

	public AnvilEvent() {
		this.result = ActionResult.PASS;
	}

	public AnvilEvent fire() {
		return Anvil.fire(this);
	}

	public ActionResult getResult() {
		return this.result;
	}

	public void setAccepted() {
		if (!this.isAccepted()) {
			this.setConsume();
		}
	}

	public boolean isAccepted() {
		return this.result.isAccepted();
	}

	public boolean shouldContinue() {
		return !this.isFail() && !this.isSuccess();
	}

	public boolean isFail() {
		return this.result == ActionResult.FAIL;
	}

	public boolean isPass() {
		return this.result == ActionResult.PASS;
	}

	public boolean isConsume() {
		return this.result == ActionResult.CONSUME;
	}

	public boolean isSuccess() {
		return this.result == ActionResult.SUCCESS;
	}

	public void setFail() {
		this.result = ActionResult.FAIL;
	}

	public void setPass() {
		this.result = ActionResult.PASS;
	}

	public void setConsume() {
		this.result = ActionResult.CONSUME;
	}

	public void setSuccess() {
		this.result = ActionResult.SUCCESS;
	}

	public void setResult(ActionResult result) {
		this.result = result;
	}
}
