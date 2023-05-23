package near.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("")
    public String getName() {
        try {
            Engine engine = Engine.newBuilder()
                    .option("engine.WarnInterpreterOnly", "false")
                    .build();
            Context ctx = Context.newBuilder("js").engine(engine).build();

        String code = """
const nearAPI  = require('near-api-js');
const { connect, KeyPair, keyStores, utils } = nearAPI;

//this is required if using a local .env file for private key
require('dotenv').config();

// configure accounts, network, and amount of NEAR to send
// converts NEAR amount into yoctoNEAR (10^-24) using a near-api-js utility
const sender = 'sender.testnet';
const receiver = 'receiver.testnet';
const networkId = 'testnet';
const amount = utils.format.parseNearAmount('1.5');

async function main() {
  // sets up an empty keyStore object in memory using near-api-js
  const keyStore = new keyStores.InMemoryKeyStore();
  // creates a keyPair from the private key provided in your .env file
  const keyPair = KeyPair.fromString(process.env.SENDER_PRIVATE_KEY);
  // adds the key you just created to your keyStore which can hold multiple keys
  await keyStore.setKey(networkId, sender, keyPair);

  // configuration used to connect to NEAR
  const config = {
    networkId,
    keyStore,
    nodeUrl: `https://rpc.${networkId}.near.org`,
    walletUrl: `https://wallet.${networkId}.near.org`,
    helperUrl: `https://helper.${networkId}.near.org`,
    explorerUrl: `https://explorer.${networkId}.near.org`
  };

  // connect to NEAR! :)\s
  const near = await connect(config);
  // create a NEAR account object
  const senderAccount = await near.account(sender);

  try {
    // here we are using near-api-js utils to convert yoctoNEAR back into a floating point
    console.log(`Sending ${utils.format.formatNearAmount(amount)}Ⓝ from ${sender} to ${receiver}...`);
    // send those tokens! :)
    const result = await senderAccount.sendMoney(receiver, amount);
    // console results
    console.log('Transaction Results: ', result.transaction);
    console.log('--------------------------------------------------------------------------------------------');
    console.log('OPEN LINK BELOW to see transaction in NEAR Explorer!');
    console.log(`${config.explorerUrl}/transactions/${result.transaction.hash}`);
    console.log('--------------------------------------------------------------------------------------------');
  } catch(error) {
    // return an error if unsuccessful
    console.log(error);
  }
}
// run the function
main();
""";

            ctx.eval("js", code);
        } catch (Exception e) {
            System.err.println(e);
        }
        return "hi";
        /*
        String code = """
print( Math.min(2, 3) )

""";
        try (Context context = Context.create("js")) {
            context.eval("js", code);
        } catch (Exception e) {
            System.err.println();
        }
        return "hi";
        */
    }
}
