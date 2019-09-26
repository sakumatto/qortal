package org.qora.network.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.qora.data.network.OnlineAccountData;
import org.qora.transform.Transformer;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class OnlineAccountsMessage extends Message {
	private static final int MAX_ACCOUNT_COUNT = 1000;

	private List<OnlineAccountData> onlineAccounts;

	public OnlineAccountsMessage(List<OnlineAccountData> onlineAccounts) {
		this(-1, onlineAccounts);
	}

	private OnlineAccountsMessage(int id, List<OnlineAccountData> onlineAccounts) {
		super(id, MessageType.ONLINE_ACCOUNTS);

		this.onlineAccounts = onlineAccounts;
	}

	public List<OnlineAccountData> getOnlineAccounts() {
		return this.onlineAccounts;
	}

	public static Message fromByteBuffer(int id, ByteBuffer bytes) throws UnsupportedEncodingException {
		final int accountCount = bytes.getInt();

		if (accountCount > MAX_ACCOUNT_COUNT)
			return null;

		List<OnlineAccountData> onlineAccounts = new ArrayList<>(accountCount);

		for (int i = 0; i < accountCount; ++i) {
			long timestamp = bytes.getLong();

			byte[] signature = new byte[Transformer.SIGNATURE_LENGTH];
			bytes.get(signature);

			byte[] publicKey = new byte[Transformer.PUBLIC_KEY_LENGTH];
			bytes.get(publicKey);

			OnlineAccountData onlineAccountData = new OnlineAccountData(timestamp, signature, publicKey);
			onlineAccounts.add(onlineAccountData);
		}

		return new OnlineAccountsMessage(id, onlineAccounts);
	}

	@Override
	protected byte[] toData() {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			bytes.write(Ints.toByteArray(this.onlineAccounts.size()));

			for (int i = 0; i < this.onlineAccounts.size(); ++i) {
				OnlineAccountData onlineAccountData = this.onlineAccounts.get(i);

				bytes.write(Longs.toByteArray(onlineAccountData.getTimestamp()));

				bytes.write(onlineAccountData.getSignature());

				bytes.write(onlineAccountData.getPublicKey());
			}

			return bytes.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

}
